<template>
  <div style="padding: 40px;">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>会议语音实时转写</span>
        </div>
      </template>

      <el-form :model="mainForm" label-width="120px">
        <el-form-item label="选择会议：">
          <el-select
              v-model="selectedBookingId"
              placeholder="请选择您要进行语音识别的会议"
              style="width: 100%;"
              filterable
              clearable
              :disabled="voiceStatus"
          >
            <el-option
                v-for="booking in userBookings"
                :key="booking.id"
                :label="`${booking.topic} (${booking.startTime} - ${booking.endTime})`"
                :value="booking.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="选择麦克风：">
          <el-select
              v-model="mainForm.chooseMicDeviceId"
              placeholder="请选择麦克风"
              :disabled="voiceStatus"
              style="width: 100%;"
          >
            <el-option
                v-for="mic in micList"
                :key="mic.deviceId"
                :label="mic.label"
                :value="mic.deviceId"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              @click="toggleRecording"
              :disabled="!mainForm.chooseMicDeviceId || !selectedBookingId"
              style="width: 100%;"
              size="large"
          >
            {{ voiceStatus ? '停止识别' : '开始识别' }}
          </el-button>
        </el-form-item>

        <el-form-item label="实时转写结果：">
          <div class="transcript-area" ref="transcriptAreaRef">
            <div v-for="(item, index) in transcriptList" :key="index" class="transcript-item">
              <strong :class="{ 'is-me': item.username === localUserInfo.username }">{{ item.username }}: </strong>
              <span>{{ item.text }}</span>
            </div>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
/* 样式与人脸识别组件保持一致，可复用 */
.box-card {
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
}
.card-header {
  text-align: center;
  font-size: 1.2rem;
  font-weight: bold;
}
.transcript-area {
  width: 100%;
  height: 200px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  overflow-y: auto;
  background-color: #f9f9f9;
  line-height: 1.6;
}
.transcript-item {
  margin-bottom: 5px;
}
.is-me {
  color: #409EFF;
}
</style>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue';
import RecordRTC from 'recordrtc';
import { ElMessage } from 'element-plus';
// 假设您有一个API文件来获取会议列表
import { getUserBookingsAttend } from "@/api/booking";

// 1. 新增：与人脸识别组件一致的状态变量
const localUserInfo = ref(null);
const userBookings = ref([]);
const selectedBookingId = ref(null);

const micList = ref([]);
const voiceStatus = ref(false);
const stream = ref(null);
const recorder = ref(null);
const socket = ref(null);
const transcriptAreaRef = ref(null);
const transcriptList = ref([]);

const mainForm = reactive({
  chooseMicDeviceId: '',
});

// 2. 新增：从API获取用户可参加的会议列表
const fetchUserBookings = async () => {
  try {
    const res = await getUserBookingsAttend(); // 复用人脸识别的API
    if (res.code === 1) {
      // 过滤出未结束的会议
      userBookings.value = (res.data || []).filter(b => new Date(b.endTime) > new Date());
    } else {
      ElMessage.error(res.msg || '获取会议列表失败');
    }
  } catch (error) {
    console.error('获取会议列表异常:', error);
  }
};

const getMicDevices = async () => {
  try {
    // 预请求一次以获取权限
    const tempStream = await navigator.mediaDevices.getUserMedia({ audio: true });
    const devices = await navigator.mediaDevices.enumerateDevices();
    micList.value = devices.filter(d => d.kind === 'audioinput');
    if(micList.value.length > 0) {
      mainForm.chooseMicDeviceId = micList.value[0].deviceId;
    }
    // 立即释放临时流
    tempStream.getTracks().forEach(track => track.stop());
  } catch (e) {
    ElMessage.error('无法获取麦克风权限，请检查浏览器设置。');
    console.error('无法获取麦克风设备:', e);
  }
};

const toggleRecording = () => {
  voiceStatus.value ? stopRecording() : startRecording();
};

const startRecording = async () => {
  if (voiceStatus.value) return;

  // 4. 核心修改：使用动态信息构建WebSocket URL
  const meetingId = selectedBookingId.value;
  const user = localUserInfo.value;

  if (!meetingId || !user) {
    ElMessage.error('请先选择会议或确认登录状态。');
    return;
  }

  try {
    stream.value = await navigator.mediaDevices.getUserMedia({
      audio: { deviceId: mainForm.chooseMicDeviceId }
    });

    const url = `ws://localhost:8080/audio/${meetingId}/${user.id}/${user.username}`;
    socket.value = new WebSocket(url);
    console.log("尝试连接WebSocket: ", url);
    socket.value.binaryType = 'arraybuffer'
    socket.value.onopen = () => {
      console.log('✅ 语音识别WebSocket已连接');
      voiceStatus.value = true;
      ElMessage.success('已连接，您可以开始说话了');
    };

    socket.value.onmessage = (e) => {
      // 只处理最终结果的逻辑保持不变
      try {
        const result = JSON.parse(e.data);
        if (result.text) {
          transcriptList.value.push({
            username: result.username,
            text: result.text
          });
          nextTick(() => {
            if (transcriptAreaRef.value) {
              transcriptAreaRef.value.scrollTop = transcriptAreaRef.value.scrollHeight;
            }
          });
        }
      } catch (err) {
        console.error('解析消息失败:', e.data, err);
      }
    };

    socket.value.onerror = (e) => {
      console.error('WebSocket 错误:', e);
      ElMessage.error('WebSocket连接发生错误。');
      stopRecording(); // 出错时也清理资源
    };

    socket.value.onclose = () => {
      console.log('WebSocket 连接已关闭');
      // onclose时调用stopRecording可以防止在stopRecording之外关闭时资源不被清理
      stopRecording();
    };

    recorder.value = RecordRTC(stream.value, {
      type: 'audio',
      mimeType: 'audio/wav',
      recorderType: RecordRTC.StereoAudioRecorder,
      desiredSampRate: 16000,
      numberOfAudioChannels: 1,
      timeSlice: 100,
      ondataavailable: (blob) => {
        if (socket.value && socket.value.readyState === WebSocket.OPEN) {
          console.log('send audio');
          socket.value.send(blob);
        }
      }
    });

    recorder.value.startRecording();

  } catch (err) {
    console.error('开始录音失败:', err);
    ElMessage.error('启动麦克风失败，请重试。');
  }
};

const stopRecording = () => {
  if (!voiceStatus.value && !socket.value) return; // 防止重复执行

  voiceStatus.value = false;

  if (recorder.value) {
    recorder.value.stopRecording(() => {
      stream.value?.getTracks().forEach(t => t.stop());
      stream.value = null;
      recorder.value = null;
      console.log('录音和媒体流已停止');
    });
  } else {
    stream.value?.getTracks().forEach(t => t.stop());
    stream.value = null;
  }

  if (socket.value && socket.value.readyState === WebSocket.OPEN) {
    socket.value.close();
  }
  socket.value = null;
};

// 3. 修改：在组件挂载时，从localStorage获取用户信息，并拉取会议列表
onMounted(() => {
  try {
    const savedUserInfo = localStorage.getItem('userInfo');
    if (savedUserInfo) {
      localUserInfo.value = JSON.parse(savedUserInfo);
      fetchUserBookings(); // 获取会议列表
    } else {
      ElMessage.error('无法在本地找到用户信息，请先登录。');
    }
  } catch(e) {
    console.error("解析用户信息失败", e);
    ElMessage.error("本地用户信息格式错误。");
  }
  getMicDevices();
});

onUnmounted(() => {
  stopRecording();
});

</script>
<template>
  <div class="face-signin-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>会议签到与人脸管理</span>
        </div>
      </template>

      <el-form label-width="100px">
        <el-form-item label="选择会议：">
          <el-select
              v-model="selectedBookingId"
              placeholder="请选择您要签到的会议"
              style="width: 100%;"
              filterable
              clearable
          >
            <el-option
                v-for="booking in userBookings"
                :key="booking.id"
                :label="`${booking.topic} (${booking.startTime} - ${booking.endTime})`"
                :value="booking.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="签到状态：" v-if="selectedBooking">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="应到人数">{{ requiredAttendeesCount }} 人</el-descriptions-item>
            <el-descriptions-item label="已到人数">{{ actualAttendeesList.length }} 人</el-descriptions-item>
            <el-descriptions-item label="已签到列表" :span="2">
              <el-tag v-if="actualAttendeesList.length === 0" type="info">暂无签到</el-tag>
              <el-tag v-for="name in actualAttendeeNames" :key="name" style="margin-right: 5px;">
                {{ name }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-form-item>

        <el-form-item label="选择设备：">
          <el-select
              v-model="selectedDeviceId"
              placeholder="请选择摄像头"
              @change="startCamera"
              style="width: 100%;"
              :disabled="isLoading"
          >
            <el-option
                v-for="device in cameraList"
                :key="device.deviceId"
                :label="device.label || `摄像头 ${device.deviceId}`"
                :value="device.deviceId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="实时画面：">
          <div class="video-container">
            <video ref="videoElement" autoplay playsinline muted></video>
            <div v-if="!isCameraOn" class="video-placeholder">
              <el-icon><CameraFilled /></el-icon>
              <span>请选择一个摄像头设备以开始</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <div class="button-group">
            <el-button
                type="success"
                @click="handleUpload"
                :disabled="!isCameraOn || isLoading"
                :loading="isLoading"
                size="large"
            >
              上传人脸信息
            </el-button>
            <el-button
                type="primary"
                @click="handleSignIn"
                :disabled="!isCameraOn || isLoading || !selectedBookingId"
                :loading="isLoading"
                size="large"
            >
              点击进行签到
            </el-button>
          </div>
        </el-form-item>
      </el-form>

      <canvas ref="canvasElement" style="display: none;"></canvas>

    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted,computed  } from 'vue';
import request from '@/utils/request';
import { ElMessage } from 'element-plus';
import { CameraFilled } from '@element-plus/icons-vue';
import {getUserBookingsAttend} from "@/api/booking";

const localUserInfo = ref(null);
// Vue Refs
const cameraList = ref([]);
const selectedDeviceId = ref('');
const videoElement = ref(null);
const canvasElement = ref(null);
const stream = ref(null);

// 状态管理
const isCameraOn = ref(false);
const isLoading = ref(false);

const userBookings = ref([]);
const selectedBookingId = ref(null);
const allUsers = ref([]); // 用于ID到姓名的转换
// --- 计算属性 ---
const selectedBooking = computed(() => {
  return userBookings.value.find(b => b.id === selectedBookingId.value) || null;
});

const requiredAttendeesCount = computed(() => {
  if (!selectedBooking.value || !selectedBooking.value.attendees) return 0;
  return selectedBooking.value.attendees.split(',').filter(id => id).length;
});

const actualAttendeesList = computed(() => {
  if (!selectedBooking.value || !selectedBooking.value.actualAttendees) return [];
  return selectedBooking.value.actualAttendees.split(',').filter(id => id);
});

const actualAttendeeNames = computed(() => {
  if (!allUsers.value.length || !actualAttendeesList.value.length) return actualAttendeesList.value;
  return actualAttendeesList.value.map(id => {
    const user = allUsers.value.find(u => u.id == id);
    return user ? user.username : `用户ID-${id}`;
  });
});


// --- API 调用与逻辑处理 ---

const fetchUserBookings = async () => {
  try {
    const res = await getUserBookingsAttend();
    if (res.code === 1) {
      userBookings.value = (res.data || []).filter(b => new Date(b.endTime) > new Date());
    } else {
      ElMessage.error(res.msg || '获取预约列表失败');
    }
  } catch (error) {
    console.error('获取预约列表异常:', error);
  }
};

const fetchAllUsers = async () => {
  try {
    const res = await request.get('/user/list');
    if (res.code === 1) {
      allUsers.value = res.data;
    }
  } catch (error) {
    console.error('获取所有用户列表异常:', error);
  }
};
// 获取所有视频输入设备（摄像头）
const getCameras = async () => {
  try {
    await navigator.mediaDevices.getUserMedia({ video: true, audio: false });
    const devices = await navigator.mediaDevices.enumerateDevices();
    cameraList.value = devices.filter(device => device.kind === 'videoinput');
    if (cameraList.value.length > 0) {
      selectedDeviceId.value = cameraList.value[0].deviceId;
      await startCamera();
    } else {
      ElMessage.warning('未检测到摄像头设备。');
    }
  } catch (err) {
    console.error('获取摄像头权限或列表失败:', err);
    ElMessage.error('无法获取摄像头权限，请检查浏览器设置。');
  }
};

// 停止当前的摄像头流
const stopCamera = () => {
  if (stream.value) {
    stream.value.getTracks().forEach(track => track.stop());
    isCameraOn.value = false;
    stream.value = null;
  }
};

// 启动选择的摄像头
const startCamera = async () => {
  stopCamera();
  if (!selectedDeviceId.value || !videoElement.value) return;

  try {
    const constraints = { video: { deviceId: { exact: selectedDeviceId.value } } };
    stream.value = await navigator.mediaDevices.getUserMedia(constraints);
    videoElement.value.srcObject = stream.value;
    isCameraOn.value = true;
  } catch (err) {
    console.error(`启动摄像头 ${selectedDeviceId.value} 失败:`, err);
    ElMessage.error('启动摄像头失败，请尝试其他设备或检查权限。');
  }
};

// --- 新增：重构的辅助函数，用于捕获图像并返回FormData ---
const captureAndGetFormData = () => {
  return new Promise((resolve, reject) => {
    const video = videoElement.value;
    const canvas = canvasElement.value;
    if (!video || !canvas) {
      return reject(new Error('视频或画布元素未准备好'));
    }
    const context = canvas.getContext('2d');

    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    context.drawImage(video, 0, 0, canvas.width, canvas.height);

    canvas.toBlob((blob) => {
      if (!blob) {
        return reject(new Error('无法捕获图像，请重试。'));
      }
      const file = new File([blob], "face-capture.jpg", { type: "image/jpeg" });
      const formData = new FormData();
      formData.append("file", file);
      resolve(formData);
    }, 'image/jpeg');
  });
};

// --- 新增：处理上传人脸信息的逻辑 ---
const handleUpload = async () => {
  if (!isCameraOn.value || isLoading.value) return;
  isLoading.value = true;

  try {
    const formData = await captureAndGetFormData();
    const userId = localUserInfo.value.id;
    if (!userId) {
      ElMessage.error('无法获取用户信息，请重新登录。');
      return;
    }

    // 调用上传API
    const response = await request.post(`/user/${userId}/upload`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });

    if (response.code === 1) {
      ElMessage.success(response.msg || '人脸信息上传成功');
    } else {
      ElMessage.error(response.msg || '人脸信息上传失败');
    }
  } catch (error) {
    console.error('上传人脸信息请求失败:', error);
    ElMessage.error(error.message || '请求失败，请检查网络或联系管理员。');
  } finally {
    isLoading.value = false;
  }
};

// --- 重构：处理签到逻辑 ---
const handleSignIn = async () => {
  if (!isCameraOn.value || isLoading.value) return;
  isLoading.value = true;

  try {
    const formData = await captureAndGetFormData();
    const userId = localUserInfo.value.id;
    if (!userId) {
      ElMessage.error('无法获取用户信息，请重新登录。');
      return;
    }

    // 调用比对API
    const response = await request.post(`/user/${userId}/faceCompare`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });

    if (response.code === 1) {
      console.log('人脸比对成功')
      ElMessage.success(response.msg || '人脸比对成功');
      // 步骤 2: 人脸比对成功后，调用签到API
      // 注意：请确保您的后端路径是 /bookingsPage
      const checkInResponse = await request.post(`/bookingsPage/${selectedBookingId.value}/check-in`);

      if (checkInResponse.code === 1) {
        ElMessage.success(checkInResponse.msg || '签到成功！');

        // ==================== 新增代码开始 ====================
        // 步骤 3: 签到成功后，手动更新前端的预约列表数据以实现界面即时刷新

        // 找到当前正在操作的这条预约记录
        const currentBooking = userBookings.value.find(b => b.id === selectedBookingId.value);

        if (currentBooking) {
          // 获取当前已签到的人员列表，如果为空则创建一个空数组
          const attendees = currentBooking.actualAttendees ? currentBooking.actualAttendees.split(',') : [];

          // 检查用户是否已经签到，防止重复添加
          if (!attendees.includes(String(userId))) {
            // 将当前用户的ID添加进去
            attendees.push(String(userId));

            // 将更新后的数组转换回字符串，并赋值回原来的对象
            // Vue 的响应式系统会监测到这个变化，并自动更新UI
            currentBooking.actualAttendees = attendees.join(',');
          }
        }
      }
        // ==================== 新增代码结束 ====================
    } else {
      console.log('人脸比对失败')
      ElMessage.error(response.msg || '人脸比对失败');
    }
  } catch (error) {
    console.error('人脸比对请求失败:', error);
    ElMessage.error(error.message || '签到请求失败，请检查网络或联系管理员。');
  } finally {
    isLoading.value = false;
  }
};


// 组件挂载时获取摄像头列表
onMounted(() => {
  // 从 localStorage 加载用户信息
  try {
    const savedUserInfo = localStorage.getItem('userInfo');
    if (savedUserInfo) {
      localUserInfo.value = JSON.parse(savedUserInfo);
      // 加载成功后，再去获取该用户的预约列表
      fetchUserBookings();
    } else {
      ElMessage.error('无法在本地找到用户信息，请先登录。');
    }
  } catch(e) {
    console.error("解析用户信息失败", e);
    ElMessage.error("本地用户信息格式错误。");
  }

  getCameras();
  fetchAllUsers();
});

// 组件卸载时停止摄像头，释放资源
onUnmounted(() => {
  stopCamera();
});

</script>

<style scoped>
.face-signin-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
}
.box-card {
  width: 100%;
  max-width: 600px;
}
.card-header {
  text-align: center;
  font-size: 1.2rem;
  font-weight: bold;
}
.video-container {
  width: 100%;
  aspect-ratio: 4 / 3;
  background-color: #000;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
}
video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.video-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #888;
}
.video-placeholder .el-icon {
  font-size: 48px;
  margin-bottom: 10px;
}
/* 新增：按钮组样式 */
.button-group {
  width: 100%;
  display: flex;
  gap: 20px; /* 按钮之间的间距 */
}
.button-group > .el-button {
  flex-grow: 1; /* 让按钮平分宽度 */
}
</style>
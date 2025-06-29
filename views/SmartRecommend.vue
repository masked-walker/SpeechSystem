<template>
  <div class="smart-recommend">
    <el-card class="filter-card">
      <template #header>
        <div class="card-header">
          <span>智能推荐会议室</span>
        </div>
      </template>
      <el-form :model="form" label-width="100px">
        <el-form-item label="使用时间" required>
          <el-date-picker
            v-model="form.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="使用人数" required>
          <el-input-number v-model="form.peopleCount" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="所需设备">
          <el-select v-model="form.equipment" multiple placeholder="请选择所需设备">
            <el-option label="投影仪" value="投影仪" />
            <el-option label="白板" value="白板" />
            <el-option label="视频会议系统" value="视频会议系统" />
            <el-option label="音响" value="音响" />
            <el-option label="电话" value="电话" />
            <el-option label="咖啡机" value="咖啡机" />
            <el-option label="智能白板" value="智能白板" />
            <el-option label="移动桌椅" value="移动桌椅" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRecommend">智能推荐</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card class="result-card" v-if="rooms.length > 0" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>推荐会议室列表</span>
        </div>
      </template>
      <el-table :data="rooms" style="width: 100%">
        <el-table-column prop="name" label="会议室名称" />
        <el-table-column prop="capacity" label="容纳人数" />
        <el-table-column prop="location" label="位置" />
        <el-table-column label="设备">
          <template #default="scope">
            <el-tag v-for="item in parseEquipment(scope.row.equipment)" :key="item">{{ item }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button type="primary" size="small" @click="openBookingDialog(scope.row)">快速预约</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-empty v-else-if="searched" description="暂无符合条件的会议室" />

    <!-- 预约弹窗 -->
    <el-dialog v-model="bookingDialogVisible" title="快速预约" width="500px" align-center destroy-on-close>
      <el-form :model="bookingForm" :rules="bookingRules" ref="bookingFormRef" label-width="100px">
        <el-form-item label="会议室">
          <el-input v-model="bookingForm.roomName" disabled />
        </el-form-item>
        <el-form-item label="使用时间">
          <el-input v-model="bookingForm.time" disabled />
        </el-form-item>
        <el-form-item label="会议主题" prop="title">
          <el-input v-model="bookingForm.title" placeholder="请输入会议主题" />
        </el-form-item>
        <el-form-item label="参会人数" prop="attendees">
          <el-input-number v-model="bookingForm.attendeesNum" :min="1" :max="selectedRoom?.capacity || 100" />
        </el-form-item>
        <el-form-item label="会议描述" prop="description">
          <el-input v-model="bookingForm.description" type="textarea" rows="3" placeholder="请输入会议描述（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bookingDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBooking" :loading="submitLoading">提交预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { addBooking } from '@/api/booking'

const form = reactive({
  timeRange: [],
  peopleCount: 1,
  equipment: []
})
const rooms = ref([])
const searched = ref(false)
const bookingDialogVisible = ref(false)
const submitLoading = ref(false)
const bookingFormRef = ref(null)
const selectedRoom = ref(null)
const bookingForm = reactive({
  roomId: '',
  roomName: '',
  time: '',
  title: '',
  attendeesNum: 1,
  description: ''
})
const bookingRules = {
  title: [
    { required: true, message: '请输入会议主题', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  attendeesNum: [
    { required: true, message: '请输入参会人数', trigger: 'blur' },
    { type: 'number', min: 1, message: '参会人数必须大于0', trigger: 'blur' }
  ]
}

function parseEquipment(equipment) {
  try {
    return JSON.parse(equipment || '[]')
  } catch {
    return []
  }
}

const handleRecommend = async () => {
  if (!form.timeRange || form.timeRange.length !== 2) {
    ElMessage.warning('请选择使用时间')
    return
  }
  if (!form.peopleCount) {
    ElMessage.warning('请输入使用人数')
    return
  }
  const [startTime, endTime] = form.timeRange
  const res = await request.post('/api/smart-recommend/rooms', {
    startTime, endTime, peopleCount: form.peopleCount, equipment: form.equipment
  })
  if (res.code === 200 || res.code === 1) {
    rooms.value = res.data
    searched.value = true
  } else {
    ElMessage.error(res.msg || '推荐失败')
  }
}

function openBookingDialog(room) {
  selectedRoom.value = room
  bookingForm.roomId = room.id
  bookingForm.roomName = room.name
  bookingForm.time = form.timeRange.length === 2 ? `${form.timeRange[0]} 至 ${form.timeRange[1]}` : ''
  bookingForm.title = ''
  bookingForm.attendeesNum = form.peopleCount || 1
  bookingForm.description = ''
  bookingDialogVisible.value = true
  nextTick(() => {
    bookingFormRef.value && bookingFormRef.value.clearValidate()
  })
}

async function submitBooking() {
  await bookingFormRef.value.validate()
  submitLoading.value = true
  try {
    const res = await addBooking({
      roomId: bookingForm.roomId,
      title: bookingForm.title,
      startTime: form.timeRange[0],
      endTime: form.timeRange[1],
      attendeesNum: String(bookingForm.attendeesNum),
      description: bookingForm.description
    })
    if (res.code === 1 || res.code === 200) {
      ElMessage.success('预约成功')
      bookingDialogVisible.value = false
    } else {
      ElMessage.error(res.msg || '预约失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || '预约失败')
  } finally {
    submitLoading.value = false
  }
}
</script>
<style scoped>
.smart-recommend {
  padding: 20px;
}
.filter-card {
  margin-bottom: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.result-card {
  margin-top: 20px;
}
</style> 
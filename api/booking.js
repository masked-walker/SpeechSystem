import request from '@/utils/request';
import {ref} from "vue";

/**
 * 获取预约列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {number} params.roomId - 会议室ID（可选）
 * @param {number} params.userId - 预约用户ID（可选）
 * @param {number} params.status - 状态（可选）
 * @param {string} params.begin - 开始时间（可选）
 * @param {string} params.end - 结束时间（可选）
 * @returns {Promise}
 */
export function getBookingList(params) {
  // 修复结束日期问题
  if (params.end) {
    // 克隆参数对象以避免修改原始对象
    const newParams = {...params};
    
    // 解析结束日期并增加1天
    if (params.end.match(/^\d{4}-\d{2}-\d{2}$/)) {
      const endDate = new Date(params.end);
      endDate.setDate(endDate.getDate() + 1);
      
      // 格式化为YYYY-MM-DD
      const year = endDate.getFullYear();
      const month = String(endDate.getMonth() + 1).padStart(2, '0');
      const day = String(endDate.getDate()).padStart(2, '0');
      newParams.end = `${year}-${month}-${day}`;
      
      console.log('API层自动调整结束日期:', params.end, '->', newParams.end);
      
      return request({
        url: '/bookingsPage',
        method: 'get',
        params: newParams
      });
    }
  }
  
  return request({
    url: '/bookingsPage',
    method: 'get',
    params
  });
}

/**
 * 获取预约详情
 * @param {number} id - 预约ID
 * @returns {Promise}
 */
export function getBookingDetail(id) {
  return request({
    url: `/bookingsPage/${id}`,
    method: 'get'
  });
}

/**
 * 添加会议室预订
 * @param {Object} data - 预约信息
 * @param {number} data.roomId - 会议室ID
 * @param {string} data.title - 会议主题
 * @param {string} data.startTime - 开始时间，格式：YYYY-MM-DD HH:MM:SS
 * @param {string} data.endTime - 结束时间，格式：YYYY-MM-DD HH:MM:SS
 * @param {string} data.attendees - 参会人员
 * @param {string} data.description - 会议描述
 * @returns {Promise}
 */
export function addBooking(data) {
  // 检查是否提供了所有必要字段
  if (!data.roomId || !data.title || !data.startTime || !data.endTime) {
    return Promise.reject({
      response: {
        data: {
          code: 0,
          msg: '缺少必要的预约信息'
        }
      }
    });
  }
  
  // 检查时间格式
  const startTimePattern = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/;
  const endTimePattern = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/;
  
  if (!startTimePattern.test(data.startTime) || !endTimePattern.test(data.endTime)) {
    return Promise.reject({
      response: {
        data: {
          code: 0,
          msg: '时间格式不正确'
        }
      }
    });
  }
  
  return request({
    url: '/bookingsPage',
    method: 'post',
    data
  }).catch(error => {
    // 捕获500错误，将其转换为业务响应
    console.error('添加预订失败:', error);

    // 如果是服务器返回的错误，保留原始消息
    if (error.response && error.response.data) {
      return {
        code: 0,
        msg: error.response.data.message || '该时间段已被预约，请选择其他时间段',
        success: false
      };
    }

    // 创建一个用户友好的错误响应
    return {
      code: 0,
      msg: '该时间段已被预约，请选择其他时间段',
      success: false
    };
  });
}

/**
 * 修改预约
 * @param {number} id - 预约ID
 * @param {Object} data - 预约信息
 * @returns {Promise}
 */
export function updateBooking(id, data) {
  return request({
    url: `/bookingsPage/${id}`,
    method: 'put',
    data
  });
}

/**
 * 取消预约
 * @param {number} id - 预约ID
 * @returns {Promise}
 */
export function cancelBooking(id) {
  return request({
    url: `/bookingsPage/${id}/cancel`,
    method: 'put'
  });
}

/**
 * 获取会议室某日预订列表
 * @param {Object} params
 * @param {number} params.roomId - 会议室ID
 * @param {string} params.date - 日期，格式：YYYY-MM-DD
 * @returns {Promise}
 */
export function getBookingsByRoom(params) {
  return request({
    url: '/bookingsPage',
    method: 'get',
    params
  });
}

/**
 * 检查时间段是否可用
 * @param {Object} data
 * @param {number} data.roomId - 会议室ID
 * @param {string} data.startTime - 开始时间，格式：YYYY-MM-DD HH:MM:SS
 * @param {string} data.endTime - 结束时间，格式：YYYY-MM-DD HH:MM:SS
 * @returns {Promise}
 */
export function checkTimeSlot(data) {
  return request({
    url: '/bookingsPage/check',
    method: 'post',
    data
  });
}

/**
 * 获取用户的预订列表
 * @param {Object} params
 * @param {number} params.status - 预订状态，0:待审批, 1:已批准, 2:已拒绝, 3:已取消
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页条数
 * @returns {Promise}
 */
// 获取当前用户的预约列表
export function getUserBookings() {
  const localUserInfo = ref(null);
  try {
    const savedUserInfo = localStorage.getItem('userInfo');
    if (savedUserInfo) {
      localUserInfo.value = JSON.parse(savedUserInfo);
    }
  } catch(e) {
    console.error("解析用户信息失败", e);
  }
  const userId = localUserInfo.value.id;
  if (!userId) {
    // 如果没有userId，可以返回一个空的Promise或抛出错误
    return Promise.reject('用户未登录');
  }
  return request({
    // 路径为 /bookings，并带上userId作为查询参数
    url: '/bookings',
    method: 'get',
    params: {
      userId: userId,
    }
  });
}


/**
 * 获取待审批的预订列表
 * @param {Object} params
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页条数
 * @returns {Promise}
 */
export function getPendingBookings(params) {
  return request({
    url: '/bookingsPage/pending',
    method: 'get',
    params
  });
}

/**
 * 审批预订
 * @param {Object} data
 * @param {number} data.id - 预订ID
 * @param {number} data.status - 审批结果，1:批准, 2:拒绝
 * @param {string} data.comment - 审批意见
 * @returns {Promise}
 */
export function approveBooking(data) {
  return request({
    url: `/approvals/${data.id}`,
    method: 'put',
    data: {
      status: data.status,
      comment: data.comment
    }
  });
}

// 获取当前用户的预约列表 (作为参会者)
export function getUserBookingsAttend() {
  // 不再需要手动传递userId，因为后端会从token中解析
  return request({
    url: '/bookingsPage/my-list', // <-- 修改为新的API路径
    method: 'get'
  });
}

// 为指定预约进行签到
export function checkInForBooking(bookingId) {
  return request({
    // 路径为 /bookings/{bookingId}/check-in
    url: `/bookingsPage/${bookingId}/check-in`,
    method: 'post'
  });
}
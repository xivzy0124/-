import axios from "axios";

const service = axios.create({
  // baseURL: '/api', // 服务器专用版
  // baseURL: 'http://47.117.34.210:8080/', // 设置基础URL
  baseURL: 'http://localhost:8080/', // 设置基础URL
  timeout: 15000,// 设置请求超时时间
});

// Response interceptors
service.interceptors.response.use(
  (response) => {
    const res = response.data;

    if (response.status !== 200) {
      const apiUrl = response.config?.url || '未知接口';
      console.error(`[${apiUrl}] 服务器忙,请稍后再试~`);
      return Promise.reject(new Error("服务器忙,请稍后再试~"));
    }

    if (res.code !== 200) {
      const apiUrl = response.config?.url || '未知接口';
      console.error(`[${apiUrl}] ${res.message || "请求失败"}`);
      return Promise.reject(new Error(res.message || "请求失败"));
    }

    return res.data;
  },
  (error) => {
    const apiUrl = error.config?.url || '未知接口';
    const errorMessage = error.response?.data?.message || error.message || "网络错误";
    console.error(`[${apiUrl}] ${errorMessage},请检查网络连接`);
    return Promise.reject(error);
  }
);

export default service;
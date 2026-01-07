import axios from "axios";
import {
  ElMessage
} from "element-plus";
import router from "../router/router";


const service = axios.create({
  // baseURL: '/api', // 服务器专用版
  // baseURL: 'http://47.117.34.210:8080/', // 设置基础URL
  baseURL: 'http://localhost:8432/', // 设置基础URL
  timeout: 15000,// 设置请求超时时间
});

// Response interceptors
service.interceptors.response.use(
  (response) => {
    if (response.status !== 200) {
      ElMessage({
        type: "error",
        message: "服务器忙,请稍后再试~",
      });
      return;
    }
    return response;
  },
  (error) => {
    // do something
    return Promise.reject(error);
  }
);

export default service;
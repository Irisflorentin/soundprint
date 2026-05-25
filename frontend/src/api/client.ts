import axios, { type AxiosInstance, type AxiosResponse, type AxiosError } from 'axios';
import { ElMessage } from 'element-plus';
import type { Result } from './types';

const client: AxiosInstance = axios.create({
  baseURL: '/api',           // 通过 Vite 代理转发到 8080
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
});

// 请求拦截器
client.interceptors.request.use(
  (config) => {
    // Phase 4 之后加 JWT，目前不动
    return config;
  },
  (error) => Promise.reject(error)
);

// 响应拦截器：剥壳 + 错误提示
client.interceptors.response.use(
  (response: AxiosResponse<Result<unknown>>) => {
    // 文件下载等二进制响应直接返回原始 response
    if (response.config.responseType === 'blob') {
      return response as unknown as AxiosResponse;
    }
    const result = response.data;
    if (result.code === 200) {
      return result.data as unknown as AxiosResponse;   // 直接返回 data，调用方不用每次 .data.data
    }
    ElMessage.error(result.message || '请求失败');
    return Promise.reject(new Error(result.message));
  },
  (error: AxiosError<Result<unknown>>) => {
    const msg = error.response?.data?.message || error.message || '网络错误';
    ElMessage.error(msg);
    return Promise.reject(error);
  }
);

export default client;

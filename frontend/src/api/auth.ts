import request from '@/utils/request'
import type { Result, LoginRequest, LoginResponse, RegisterRequest, UserDTO } from './types'

export function login(data: LoginRequest): Promise<Result<LoginResponse>> {
  return request.post('/auth/login', data)
}

export function register(data: RegisterRequest): Promise<Result<void>> {
  return request.post('/auth/register', data)
}

export function getCurrentUser(): Promise<Result<UserDTO>> {
  return request.get('/users/me')
}

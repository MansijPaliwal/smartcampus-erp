import apiClient from './apiClient';
import { LoginResponse } from '../types/api';

export class AuthService {
  static async login(email: string, password: string): Promise<LoginResponse> {
    const response = await apiClient.post<LoginResponse>('/api/auth/login', {
      email,
      password
    });
    
    if (response.data && response.data.token) {
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('jwtToken', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data));
    }
    
    return response.data;
  }

  static logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('user');
  }

  static getToken(): string | null {
    return localStorage.getItem('jwtToken') || localStorage.getItem('token');
  }

  static getCurrentUser(): LoginResponse | null {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  }

  static isAuthenticated(): boolean {
    return this.getToken() !== null;
  }
}

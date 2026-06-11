import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useServices } from '../app/ServicesProvider';
import { Credentials } from '../auth/authService';
import { useSessionStore } from '../store/sessionStore';

/** Login, cadastro e logout ligando o {@link AuthService} ao estado de sessao. */
export function useAuth() {
  const { auth } = useServices();
  const queryClient = useQueryClient();
  const signIn = useSessionStore((s) => s.signIn);
  const signOut = useSessionStore((s) => s.signOut);

  const login = useMutation({
    mutationFn: (credentials: Credentials) => auth.login(credentials),
    onSuccess: () => signIn(),
  });

  const register = useMutation({
    mutationFn: async (credentials: Credentials) => {
      await auth.register(credentials);
      await auth.login(credentials);
    },
    onSuccess: () => signIn(),
  });

  const logout = () => {
    auth.logout();
    queryClient.clear();
    signOut();
  };

  return { login, register, logout };
}

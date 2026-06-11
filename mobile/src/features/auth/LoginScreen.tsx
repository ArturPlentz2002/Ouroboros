import React, { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { ApiError } from '../../api/client';
import { colors, spacing, typography } from '../../design/tokens';
import { useAuth } from '../../hooks/useAuth';
import { Button } from '../../ui/Button';
import { Input } from '../../ui/Input';

/** Login e cadastro por e-mail/senha. Login social entra com o alvo nativo. */
export function LoginScreen(): React.ReactElement {
  const { login, register } = useAuth();
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const action = mode === 'login' ? login : register;
  const submit = () => {
    if (email.trim() && password) {
      action.mutate({ email: email.trim(), password });
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Ouroboros</Text>
      <Text style={styles.subtitle}>
        {mode === 'login' ? 'Entre na sua conta' : 'Crie sua conta'}
      </Text>

      <Input
        label="E-mail"
        value={email}
        onChangeText={setEmail}
        placeholder="voce@exemplo.com"
        keyboardType="email-address"
        testID="login-email"
      />
      <Input
        label="Senha"
        value={password}
        onChangeText={setPassword}
        placeholder="minimo 8 caracteres"
        secureTextEntry
        error={errorMessage(action.error)}
        testID="login-password"
      />

      <Button
        title={mode === 'login' ? 'Entrar' : 'Cadastrar e entrar'}
        onPress={submit}
        loading={action.isPending}
        testID="login-submit"
      />
      <View style={styles.toggle}>
        <Button
          title={mode === 'login' ? 'Criar uma conta' : 'Já tenho conta'}
          onPress={() => setMode(mode === 'login' ? 'register' : 'login')}
          variant="ghost"
          testID="login-toggle"
        />
      </View>
    </View>
  );
}

function errorMessage(error: unknown): string | undefined {
  if (!error) {
    return undefined;
  }
  if (error instanceof ApiError) {
    if (error.status === 401) {
      return 'E-mail ou senha incorretos';
    }
    if (error.status === 409) {
      return 'Já existe uma conta com esse e-mail';
    }
    if (error.status === 400) {
      return 'Dados inválidos: confira e-mail e senha (mínimo 8 caracteres)';
    }
  }
  return 'Não foi possível conectar. Tente novamente.';
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    padding: spacing.lg,
    backgroundColor: colors.background,
  },
  title: {
    fontSize: typography.fontSizes.heading,
    fontWeight: typography.weights.bold,
    color: colors.primary,
    textAlign: 'center',
  },
  subtitle: {
    fontSize: typography.fontSizes.subtitle,
    color: colors.textMuted,
    textAlign: 'center',
    marginBottom: spacing.xl,
    marginTop: spacing.xs,
  },
  toggle: {
    marginTop: spacing.sm,
  },
});

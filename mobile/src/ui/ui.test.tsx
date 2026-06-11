/** @jest-environment jsdom */
import { fireEvent, render, screen } from '@testing-library/react';
import React from 'react';
import { Text } from 'react-native';
import { Button } from './Button';
import { Card } from './Card';
import { Chip } from './Chip';
import { EmptyState } from './EmptyState';
import { Input } from './Input';

describe('Button', () => {
  it('dispara onPress', () => {
    const onPress = jest.fn();
    render(<Button title="Salvar" onPress={onPress} testID="btn" />);

    fireEvent.click(screen.getByTestId('btn'));
    expect(onPress).toHaveBeenCalledTimes(1);
  });

  it('em loading nao dispara e esconde o titulo', () => {
    const onPress = jest.fn();
    render(<Button title="Salvar" onPress={onPress} loading testID="btn" />);

    fireEvent.click(screen.getByTestId('btn'));
    expect(onPress).not.toHaveBeenCalled();
    expect(screen.queryByText('Salvar')).toBeNull();
  });
});

describe('Input', () => {
  it('propaga digitacao e mostra erro', () => {
    const onChangeText = jest.fn();
    render(
      <Input
        label="E-mail"
        value=""
        onChangeText={onChangeText}
        error="obrigatorio"
        testID="inp"
      />,
    );

    fireEvent.change(screen.getByTestId('inp'), { target: { value: 'a@b.c' } });
    expect(onChangeText).toHaveBeenCalledWith('a@b.c');
    expect(screen.getByText('obrigatorio')).toBeTruthy();
  });
});

describe('Chip', () => {
  it('alterna selecao via onPress', () => {
    const onPress = jest.fn();
    render(<Chip label="tag" onPress={onPress} testID="chip" />);

    fireEvent.click(screen.getByTestId('chip'));
    expect(onPress).toHaveBeenCalled();
  });
});

describe('Card e EmptyState', () => {
  it('renderizam conteudo e dica', () => {
    render(
      <Card testID="card">
        <Text>conteudo</Text>
      </Card>,
    );
    render(<EmptyState title="Nada aqui" hint="crie o primeiro item" />);

    expect(screen.getByText('conteudo')).toBeTruthy();
    expect(screen.getByText('Nada aqui')).toBeTruthy();
    expect(screen.getByText('crie o primeiro item')).toBeTruthy();
  });
});

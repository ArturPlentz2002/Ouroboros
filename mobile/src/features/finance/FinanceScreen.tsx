import React, { useState } from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { colors, spacing, typography } from '../../design/tokens';
import {
  useCreateFinanceEntry,
  useFinanceEntries,
  useMonthlySummary,
  useRemoveFinanceEntry,
} from '../../hooks/useFinance';
import { Button } from '../../ui/Button';
import { Card } from '../../ui/Card';
import { Chip } from '../../ui/Chip';
import { EmptyState } from '../../ui/EmptyState';
import { Input } from '../../ui/Input';
import { EntryType, FinanceEntry } from './financeApi';

/** Resumo do mes + lancamentos financeiros. */
export function FinanceScreen(): React.ReactElement {
  const summary = useMonthlySummary();
  const entries = useFinanceEntries();
  const create = useCreateFinanceEntry();
  const remove = useRemoveFinanceEntry();

  const [type, setType] = useState<EntryType>('EXPENSE');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [occurredOn, setOccurredOn] = useState('');
  const [formError, setFormError] = useState<string | undefined>();

  const submit = () => {
    const value = Number(amount.replace(',', '.'));
    if (!Number.isFinite(value) || value <= 0) {
      setFormError('Informe um valor maior que zero');
      return;
    }
    if (!/^\d{4}-\d{2}-\d{2}$/.test(occurredOn.trim())) {
      setFormError('Data inválida — use AAAA-MM-DD');
      return;
    }
    setFormError(undefined);
    create.mutate(
      {
        type,
        amount: value,
        description: description.trim() || undefined,
        occurredOn: occurredOn.trim(),
      },
      {
        onSuccess: () => {
          setAmount('');
          setDescription('');
          setOccurredOn('');
        },
      },
    );
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Text style={styles.heading}>Finanças</Text>

      {summary.data ? (
        <Card testID="finance-summary">
          <Text style={styles.itemTitle}>Resumo de {summary.data.month}</Text>
          <View style={styles.summaryRow}>
            <Text style={styles.income}>Receitas: {formatMoney(summary.data.totalIncome)}</Text>
            <Text style={styles.expense}>Gastos: {formatMoney(summary.data.totalExpense)}</Text>
          </View>
          <Text style={styles.balance}>Saldo: {formatMoney(summary.data.balance)}</Text>
        </Card>
      ) : null}

      <Card>
        <View style={styles.typeRow}>
          <Chip
            label="Gasto"
            selected={type === 'EXPENSE'}
            onPress={() => setType('EXPENSE')}
            testID="finance-type-expense"
          />
          <Chip
            label="Receita"
            selected={type === 'INCOME'}
            onPress={() => setType('INCOME')}
            testID="finance-type-income"
          />
        </View>
        <Input
          label="Valor"
          value={amount}
          onChangeText={setAmount}
          placeholder="150.50"
          keyboardType="numeric"
          testID="finance-amount"
        />
        <Input
          label="Descrição"
          value={description}
          onChangeText={setDescription}
          placeholder="opcional"
          testID="finance-description"
        />
        <Input
          label="Data"
          value={occurredOn}
          onChangeText={setOccurredOn}
          placeholder="2026-06-10"
          error={formError}
          testID="finance-occurred-on"
        />
        <Button
          title="Lançar"
          onPress={submit}
          loading={create.isPending}
          testID="finance-create"
        />
      </Card>

      {entries.isLoading ? <Text style={styles.muted}>Carregando…</Text> : null}
      {entries.isError ? (
        <Text style={styles.error}>Não foi possível carregar os lançamentos.</Text>
      ) : null}
      {entries.data?.length === 0 ? (
        <EmptyState title="Sem lançamentos" hint="Registre o primeiro gasto ou receita" />
      ) : null}

      {entries.data?.map((entry: FinanceEntry) => (
        <Card key={entry.id} testID={`finance-entry-${entry.id}`}>
          <View style={styles.summaryRow}>
            <Text style={entry.type === 'INCOME' ? styles.income : styles.expense}>
              {entry.type === 'INCOME' ? '+' : '-'} {formatMoney(entry.amount)}
            </Text>
            <Text style={styles.muted}>{entry.occurredOn}</Text>
          </View>
          {entry.description ? <Text style={styles.body}>{entry.description}</Text> : null}
          <View style={styles.actions}>
            <Button
              title="Excluir"
              variant="danger"
              onPress={() => remove.mutate(entry.id)}
              testID={`finance-remove-${entry.id}`}
            />
          </View>
        </Card>
      ))}
    </ScrollView>
  );
}

function formatMoney(value: number): string {
  return `R$ ${value.toFixed(2).replace('.', ',')}`;
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  content: {
    padding: spacing.md,
  },
  heading: {
    fontSize: typography.fontSizes.title,
    fontWeight: typography.weights.bold,
    color: colors.text,
    marginBottom: spacing.md,
  },
  itemTitle: {
    fontSize: typography.fontSizes.subtitle,
    fontWeight: typography.weights.medium,
    color: colors.text,
  },
  summaryRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: spacing.xs,
  },
  income: {
    color: colors.success,
    fontWeight: typography.weights.medium,
  },
  expense: {
    color: colors.danger,
    fontWeight: typography.weights.medium,
  },
  balance: {
    marginTop: spacing.xs,
    fontSize: typography.fontSizes.subtitle,
    fontWeight: typography.weights.bold,
    color: colors.text,
  },
  typeRow: {
    flexDirection: 'row',
    marginBottom: spacing.sm,
  },
  body: {
    marginTop: spacing.xs,
    color: colors.text,
  },
  muted: {
    color: colors.textMuted,
    fontSize: typography.fontSizes.body,
  },
  error: {
    color: colors.danger,
  },
  actions: {
    marginTop: spacing.sm,
    alignSelf: 'flex-start',
  },
});

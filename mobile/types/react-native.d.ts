/**
 * Tipos minimos para `react-native` no alvo web (react-native-web nao publica tipos).
 * Cobre a superficie usada pelo app; cresce conforme novas telas/componentes entram.
 * Quando o alvo nativo (react-native) for adicionado, ele substitui este shim.
 */
declare module 'react-native' {
  import * as React from 'react';

  export type ColorValue = string;
  export interface ViewStyle {
    [key: string]: unknown;
  }
  export interface TextStyle {
    [key: string]: unknown;
  }
  export interface ImageStyle {
    [key: string]: unknown;
  }
  /** Recursivo como no RN real: arrays podem conter entradas condicionais (`x && style`). */
  export type StyleProp<T> = T | ReadonlyArray<StyleProp<T>> | null | undefined | false;

  export interface ViewProps {
    style?: StyleProp<ViewStyle>;
    children?: React.ReactNode;
    testID?: string;
    [key: string]: unknown;
  }
  export const View: React.ComponentType<ViewProps>;
  export const ScrollView: React.ComponentType<ViewProps>;
  export const SafeAreaView: React.ComponentType<ViewProps>;

  export interface TextProps {
    style?: StyleProp<TextStyle>;
    children?: React.ReactNode;
    numberOfLines?: number;
    onPress?: () => void;
    testID?: string;
    [key: string]: unknown;
  }
  export const Text: React.ComponentType<TextProps>;

  export interface TextInputProps {
    style?: StyleProp<TextStyle>;
    value?: string;
    placeholder?: string;
    placeholderTextColor?: ColorValue;
    secureTextEntry?: boolean;
    autoCapitalize?: 'none' | 'sentences' | 'words' | 'characters';
    keyboardType?: string;
    onChangeText?: (text: string) => void;
    testID?: string;
    [key: string]: unknown;
  }
  export const TextInput: React.ComponentType<TextInputProps>;

  export interface PressableProps {
    style?: StyleProp<ViewStyle> | ((state: { pressed: boolean }) => StyleProp<ViewStyle>);
    onPress?: () => void;
    disabled?: boolean;
    children?: React.ReactNode;
    testID?: string;
    [key: string]: unknown;
  }
  export const Pressable: React.ComponentType<PressableProps>;
  export const TouchableOpacity: React.ComponentType<PressableProps>;

  export interface ActivityIndicatorProps {
    size?: 'small' | 'large' | number;
    color?: ColorValue;
    [key: string]: unknown;
  }
  export const ActivityIndicator: React.ComponentType<ActivityIndicatorProps>;

  export interface FlatListProps<ItemT> {
    data: ReadonlyArray<ItemT> | null | undefined;
    renderItem: (info: { item: ItemT; index: number }) => React.ReactElement | null;
    keyExtractor?: (item: ItemT, index: number) => string;
    style?: StyleProp<ViewStyle>;
    ListEmptyComponent?: React.ReactElement | React.ComponentType | null;
    [key: string]: unknown;
  }
  export class FlatList<ItemT = unknown> extends React.Component<FlatListProps<ItemT>> {}

  type NamedStyles<T> = { [P in keyof T]: ViewStyle | TextStyle | ImageStyle };
  export const StyleSheet: {
    create<T extends NamedStyles<T> | NamedStyles<unknown>>(styles: T): T;
    flatten(style?: StyleProp<ViewStyle | TextStyle>): ViewStyle | TextStyle;
    readonly hairlineWidth: number;
    readonly absoluteFill: object;
  };

  export const Platform: {
    OS: 'ios' | 'android' | 'web';
    select<T>(spec: { [k: string]: T }): T | undefined;
  };

  export interface AppRegistryRunOptions {
    rootTag: Element | null;
  }
  export const AppRegistry: {
    registerComponent(appKey: string, getComponent: () => React.ComponentType): string;
    runApplication(appKey: string, options: AppRegistryRunOptions): void;
  };
}

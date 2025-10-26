declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

// Vue JSX 类型支持
declare namespace JSX {
  interface IntrinsicElements {
    [elem: string]: any
  }
}
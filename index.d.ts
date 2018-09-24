declare module 'react-native-uber-sso' {
  export interface IUberSSOResult {
    data: any | Error
    status: string
    type: string
  }
  export interface IUberSDKOptions {
    clientId: string
    environment?: string
    isDebug?: boolean
    redirectUri: string
  }
  export type UberInstallConversionDisposer = () => void
  export default class Uber {
    static initSdk(
      options: IUberSDKOptions,
      successCallback: (result: string) => void,
      errorCallback: (error: Error) => void
    ): void
    static login(): void
    static onSSOUberSSOAccessToken(
      callback: (data: IUberSSOResult) => void
    ): UberInstallConversionDisposer
  }
}

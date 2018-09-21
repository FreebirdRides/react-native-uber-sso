
# react-native-uber-sso

## Getting started

`$ npm install react-native-uber-sso --save`

### Mostly automatic installation

`$ react-native link react-native-uber-sso`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-uber-sso` and add `RNUberSso.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNUberSso.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.freebirdrides.reactnative.RNUberSsoPackage;` to the imports at the top of the file
  - Add `new RNUberSsoPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-uber-sso'
  	project(':react-native-uber-sso').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-uber-sso/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-uber-sso')
  	```


## Usage
```javascript
import RNUberSso from 'react-native-uber-sso';

// TODO: What to do with the module?
RNUberSso;
```
  
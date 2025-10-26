好的，这是您提供的中文内容的英文翻译：

## LemonPush

[Simplified Chinese](readme.md) | [English](readme-en.md)

LemonPush is an efficient tool for pushing text from your mobile device to your computer's clipboard under the same WiFi environment. It supports Android and iOS on the mobile side, and Windows, Mac, and Linux platforms on the computer side.

## [Download Link (Including iOS Shortcuts)](https://lemontree.one/sibtools/lemon_push/docs/download)

## Setup Tutorial

Double-click to launch the LemonPush application on your computer. On the Android side, install the LemonPush App; on the iOS side, use the Shortcuts. On the computer side, click LemonPush to generate a QR code. Simply scan the code with your Android phone to match and start using. For iOS, modify the IP in the Shortcut to your computer's IP address to use it.

When generating the QR code, your computer might show multiple IP addresses. Use the IP address of your local area network (LAN), typically starting with 192. After scanning the code or manually entering the computer's IP, click "Push Clipboard" to capture and push the clipboard content to the computer.

If a port conflict occurs, you can modify the port number and restart the program.

## API Documentation

### Write to Computer Clipboard

`/set_clipboard?text=content`

Returns JSON:

```
{
    "code":"0",
    "data":"ok"
}
```

### Get Computer Clipboard

`/get_clipboard`

Returns JSON:

```
{
    "code":"0",
    "data":"Computer clipboard content"
}
```

### File Upload

Files are saved in the `./_lemon_` directory.

`/upload`

Request Example:

`curl --location --request POST 'http://localhost:14756/upload' \ --form 'file=@"/E:/Downloads/__UNI__F0B72F8_0809143049.apk"'`

### File Download

`/download`

Request Example:

`curl --location --request GET 'http://localhost:14756/download?filename=__UNI__F0B72F8_0809143049.apk'`

## FAQ

  - The computer cannot receive the mobile clipboard: You need to configure the computer's firewall to allow the application to pass through.
  - Mac system currently does not support the tray icon.
  - Linux currently has no graphical interface version.

## Development Background

The frequency of sending messages between mobile phones and computers is high in daily life, and using WeChat or QQ to send messages is often a little cumbersome.

For example:

I. Traditional steps for viewing a webpage link from a mobile phone on a computer:

1.  Copy or share the link.
2.  Select QQ or WeChat to send.
3.  On QQ, click the link directly to open; on WeChat, you still need to copy the link to the browser.

II. Traditional steps for transferring a mobile verification code to a computer:

1.  Copy the verification code on the mobile side.
2.  Select QQ or WeChat to send.
3.  Copy the verification code on the computer side.

The pain points above have been improved by the multi-screen collaboration solutions launched by mobile phone manufacturers, but these are often limited, such as only supporting certain phones or their own brand laptops, etc.

Using LemonPush can reduce the steps above. If you enable "Push on Launch" in LemonPush, when you copy text and switch to the LemonPush app, the text will be immediately pushed to the computer clipboard. If the text contains a link, it will be automatically opened in the default browser.

The core of improving efficiency is reducing steps and reducing choices. Sending text to a computer almost certainly means copying it to the clipboard, and sending a link to a computer almost certainly means opening it in a browser. LemonPush was developed based on these assumptions.

## Development Technology

The computer side converts the clipboard interface into an HTTP service. Information exchange is achieved through a LAN-based HTTP service, and the computer-side program is implemented using the Go language.

## Known Issues

Limited by the author's development level, there are still many areas of the software that need refinement. For example, the transmission content is not encrypted, which poses a security risk. However, it is safe for most scenarios, unless someone actively attacks within the local network.

Please do not download the software from third-party platforms. The project code is open source, and downloading and using software from third-party platforms might introduce malicious code, leading to information leakage.

## Cloud Drive Download

Cloud drive download link: [https://pan.quark.cn/s/077a3ded98a5](https://pan.quark.cn/s/077a3ded98a5)

## Release Download

Please go to the release page to download the latest version: [Release Page](https://github.com/lemon-codehub/lemonPush/releases)
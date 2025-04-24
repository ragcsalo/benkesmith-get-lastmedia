# benkesmith-get-lastmedia

[![MIT License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

A Cordova plugin for **fetching the last N photos/videos** from the phone. Works on both **Android** and **iOS**.

## Features

- Fetches the latest photo(s) or video(s)
- Unified API for both Android and iOS
- Minimal permissions required
- Returns file URIs and metadata (MIME type or UTI)

## Installation

```bash
cordova plugin add https://github.com/ragcsalo/benkesmith-get-lastmedia
```

## Usage

```js
cordova.plugins.GetMedia.getLast(n,
  function(mediaArray) {
    console.log('Media:', mediaArray);
  },
  function(error) {
    console.error('Error:', error);
  }
);
```

- `n`: Number of recent photos/videos to fetch.
- Returns an array of objects like:

```js
[
  {
    uri: "content://...",
    mimeType: "image/jpeg" // Android
    // or
    uri: "file:///...", 
    uti: "public.jpeg"     // iOS
  },
  ...
]
```

## Platforms

- Android
- iOS

## Android Notes

- Uses `MediaStore` to query images and videos from external storage
- Sorts by `DATE_TAKEN DESC` and limits to `n`
- Requires `READ_EXTERNAL_STORAGE` (automatically declared)

## iOS Notes

- Uses the Photos framework
- Sorts by `creationDate` and limits to `n`
- Requires user permission to access the photo library (automatically handled by Cordova)


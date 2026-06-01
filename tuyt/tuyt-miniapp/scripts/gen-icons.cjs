// 生成 TabBar 占位图标（最小有效PNG，8x8像素，各色块）
const fs = require('fs')
const path = require('path')

const imagesDir = path.join(__dirname, '..', 'images')
if (!fs.existsSync(imagesDir)) fs.mkdirSync(imagesDir, { recursive: true })

// 最小有效PNG：8x8像素
function createPNG(r, g, b) {
  // PNG 签名
  const signature = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10])
  // IHDR
  const width = 8, height = 8
  const ihdrData = Buffer.alloc(13)
  ihdrData.writeUInt32BE(width, 0)
  ihdrData.writeUInt32BE(height, 4)
  ihdrData[8] = 8 // bit depth
  ihdrData[9] = 2 // color type (RGB)
  ihdrData[10] = 0
  ihdrData[11] = 0
  ihdrData[12] = 0

  const crc32 = (buf) => {
    let c = 0xFFFFFFFF
    for (let i = 0; i < buf.length; i++) {
      c ^= buf[i]
      for (let j = 0; j < 8; j++) c = (c >>> 1) ^ (c & 1 ? 0xEDB88320 : 0)
    }
    return (c ^ 0xFFFFFFFF) >>> 0
  }

  function chunk(type, data) {
    const len = Buffer.alloc(4)
    len.writeUInt32BE(data.length)
    const typeBuf = Buffer.from(type, 'ascii')
    const crcBuf = Buffer.alloc(4)
    const crcData = Buffer.concat([typeBuf, data])
    crcBuf.writeUInt32BE(crc32(crcData))
    return Buffer.concat([len, typeBuf, data, crcBuf])
  }

  // IDAT - raw RGB pixels (8 rows * (1 filter byte + 8*3 RGB))
  const rawData = Buffer.alloc(8 * (1 + 24))
  for (let y = 0; y < 8; y++) {
    rawData[y * 25] = 0 // filter: None
    for (let x = 0; x < 8; x++) {
      const idx = y * 25 + 1 + x * 3
      rawData[idx] = r; rawData[idx + 1] = g; rawData[idx + 2] = b
    }
  }
  const zlib = require('zlib')
  const compressed = zlib.deflateSync(rawData)

  const ihdrChunk = chunk('IHDR', ihdrData)
  const idatChunk = chunk('IDAT', compressed)
  const iendChunk = chunk('IEND', Buffer.alloc(0))

  return Buffer.concat([signature, ihdrChunk, idatChunk, iendChunk])
}

// 图标配置：文件名, RGB
const icons = [
  ['tab-home.png', 150, 150, 150],
  ['tab-home-active.png', 24, 144, 255],
  ['tab-work.png', 150, 150, 150],
  ['tab-work-active.png', 24, 144, 255],
  ['tab-problem.png', 150, 150, 150],
  ['tab-problem-active.png', 24, 144, 255],
  ['tab-message.png', 150, 150, 150],
  ['tab-message-active.png', 24, 144, 255],
  ['tab-profile.png', 150, 150, 150],
  ['tab-profile-active.png', 24, 144, 255],
]

icons.forEach(([name, r, g, b]) => {
  const png = createPNG(r, g, b)
  const filePath = path.join(imagesDir, name)
  fs.writeFileSync(filePath, png)
  console.log(`Created: ${name} (${png.length} bytes)`)
})

console.log('All tab icons generated successfully in images/')

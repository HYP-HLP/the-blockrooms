// 生成 BlackstoneShulker 实体纹理：外壳 = 黑石纹理（保留原壳明暗），内部贝 = 黑化（不太黑）。
// 用法：node docs/gen_blackstone_shulker.cjs
// 依赖：Node 内置 zlib（含 zlib.crc32，Node 22+）。
const fs = require('fs');
const zlib = require('zlib');
const path = require('path');

const SRC_SHULKER = path.join(__dirname, '..', '.tmp-tex', 'assets', 'minecraft', 'textures', 'entity', 'shulker', 'shulker.png');
const SRC_BLACKSTONE = path.join(__dirname, '..', '.tmp-tex', 'assets', 'minecraft', 'textures', 'block', 'blackstone.png');
// 染色纹理做掩码：head（内部贝）区域为白色、外壳为黑色——MC 官方就是按此区分外壳/贝
const SRC_MASK = path.join(__dirname, '..', '.tmp-tex', 'assets', 'minecraft', 'textures', 'entity', 'shulker', 'shulker_black.png');
const OUT = path.join(__dirname, '..', 'src', 'main', 'resources', 'assets', 'blockrooms', 'textures', 'entity', 'blackstone_shulker.png');

// ---------- PNG 解码（非隔行；支持 bitDepth 1/2/4/8；colorType 0/2/3/4/6） ----------
function decodePng(buf) {
  if (buf.readUInt32BE(0) !== 0x89504e47) throw new Error('not a png');
  let pos = 8;
  let width = 0, height = 0, bitDepth = 0, colorType = 0, interlace = 0;
  let palette = null, trns = null;
  const idat = [];
  while (pos < buf.length) {
    const len = buf.readUInt32BE(pos);
    const type = buf.toString('ascii', pos + 4, pos + 8);
    const data = buf.subarray(pos + 8, pos + 8 + len);
    if (type === 'IHDR') {
      width = data.readUInt32BE(0);
      height = data.readUInt32BE(4);
      bitDepth = data[8];
      colorType = data[9];
      interlace = data[12];
      if (bitDepth !== 8 && bitDepth !== 4 && bitDepth !== 2 && bitDepth !== 1) {
        throw new Error('unsupported bit depth ' + bitDepth);
      }
      if (interlace !== 0) throw new Error('interlaced png unsupported');
    } else if (type === 'PLTE') {
      palette = [];
      for (let i = 0; i < len; i += 3) palette.push([data[i], data[i + 1], data[i + 2]]);
    } else if (type === 'tRNS') {
      trns = Buffer.from(data);
    } else if (type === 'IDAT') {
      idat.push(data);
    } else if (type === 'IEND') {
      break;
    }
    pos += 12 + len;
  }
  const channels = colorType === 0 ? 1 : colorType === 2 ? 3 : colorType === 3 ? 1 : colorType === 4 ? 2 : 4;
  const raw = zlib.inflateSync(Buffer.concat(idat));
  const stride = width * channels;
  const rgba = Buffer.alloc(width * height * 4);
  const bpp = Math.max(1, channels);
  const prev = Buffer.alloc(stride);
  for (let y = 0; y < height; y++) {
    const filter = raw[y * (stride + 1)];
    const line = raw.subarray(y * (stride + 1) + 1, (y + 1) * (stride + 1));
    const recon = Buffer.alloc(stride);
    for (let x = 0; x < stride; x++) {
      const a = x >= bpp ? recon[x - bpp] : 0;
      const b = prev[x];
      const c = x >= bpp ? prev[x - bpp] : 0;
      let v = line[x];
      if (filter === 1) v += a;
      else if (filter === 2) v += b;
      else if (filter === 3) v += (a + b) >> 1;
      else if (filter === 4) {
        const p = a + b - c;
        const pa = Math.abs(p - a), pb = Math.abs(p - b), pc = Math.abs(p - c);
        v += (pa <= pb && pa <= pc) ? a : (pb <= pc ? b : c);
      }
      recon[x] = v & 0xff;
    }
    recon.copy(prev);
    for (let x = 0; x < width; x++) {
      const si = x * channels;
      let r, g, b, a = 255;
      if (colorType === 0) {
        r = g = b = recon[si];
      } else if (colorType === 2) {
        r = recon[si]; g = recon[si + 1]; b = recon[si + 2];
      } else if (colorType === 3) {
        let idx;
        if (bitDepth === 8) {
          idx = recon[si];
        } else {
          const bits = bitDepth;
          const byte = recon[Math.floor((x * bits) / 8)];
          const shift = 8 - bits - ((x * bits) % 8);
          idx = (byte >> shift) & ((1 << bits) - 1);
        }
        const p = palette[idx] || palette[palette.length - 1]; // 容错：越界索引取最后一种调色板色（原版 4bit 纹理存在此类像素）
        r = p[0]; g = p[1]; b = p[2];
        if (trns && idx < trns.length) a = trns[idx];
      } else if (colorType === 4) {
        r = g = b = recon[si]; a = recon[si + 1];
      } else {
        r = recon[si]; g = recon[si + 1]; b = recon[si + 2]; a = recon[si + 3];
      }
      const di = (y * width + x) * 4;
      rgba[di] = r; rgba[di + 1] = g; rgba[di + 2] = b; rgba[di + 3] = a;
    }
  }
  return { width, height, rgba };
}

// ---------- PNG 编码（RGBA8、filter 0、无隔行） ----------
function chunk(type, data) {
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length, 0);
  const body = Buffer.concat([Buffer.from(type, 'ascii'), data]);
  const crc = Buffer.alloc(4);
  crc.writeUInt32BE(zlib.crc32(body), 0);
  return Buffer.concat([len, body, crc]);
}

function encodePng(width, height, rgba) {
  const stride = width * 4;
  const raw = Buffer.alloc((stride + 1) * height);
  for (let y = 0; y < height; y++) {
    raw[y * (stride + 1)] = 0;
    rgba.copy(raw, y * (stride + 1) + 1, y * stride, (y + 1) * stride);
  }
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(width, 0);
  ihdr.writeUInt32BE(height, 4);
  ihdr[8] = 8; ihdr[9] = 6; ihdr[10] = 0; ihdr[11] = 0; ihdr[12] = 0;
  const out = Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
    chunk('IHDR', ihdr),
    chunk('IDAT', zlib.deflateSync(raw, { level: 9 })),
    chunk('IEND', Buffer.alloc(0)),
  ]);
  return out;
}

// ---------- 主逻辑 ----------
const lum = (r, g, b) => 0.299 * r + 0.587 * g + 0.114 * b;

const shulker = decodePng(fs.readFileSync(SRC_SHULKER));
const blackstone = decodePng(fs.readFileSync(SRC_BLACKSTONE));
const mask = decodePng(fs.readFileSync(SRC_MASK));
if (shulker.width !== 64 || shulker.height !== 64) throw new Error('shulker texture must be 64x64');

// 掩码：亮度 > MASK_T 的像素 = 内部贝（染色纹理里贝为白色、外壳为黑色）
const MASK_T = 150;
// 黑化强度：亮度乘数（0.4 = 深灰，保留细节但明显黑化）
const HEAD_DARKEN = 0.4;

let headPixels = 0, shellPixels = 0;
const out = Buffer.alloc(shulker.rgba.length);
for (let y = 0; y < 64; y++) {
  for (let x = 0; x < 64; x++) {
    const di = (y * 64 + x) * 4;
    const a = shulker.rgba[di + 3];
    if (a === 0) {
      out[di + 3] = 0; // 保持透明
      continue;
    }
    const inHead = lum(mask.rgba[di], mask.rgba[di + 1], mask.rgba[di + 2]) > MASK_T;
    if (inHead) {
      // 内部贝：去色 + 压暗（保留明暗起伏）
      const g = lum(shulker.rgba[di], shulker.rgba[di + 1], shulker.rgba[di + 2]) * HEAD_DARKEN;
      out[di] = g; out[di + 1] = g; out[di + 2] = g; out[di + 3] = a;
      headPixels++;
    } else {
      // 外壳：黑石无缝平铺（黑石为无缝纹理，按 16x16 平铺到 64x64 不会出现接缝/错位）
      const bs = (y % 16) * 16 + (x % 16);
      const bdi = bs * 4;
      out[di] = blackstone.rgba[bdi];
      out[di + 1] = blackstone.rgba[bdi + 1];
      out[di + 2] = blackstone.rgba[bdi + 2];
      out[di + 3] = a;
      shellPixels++;
    }
  }
}

fs.writeFileSync(OUT, encodePng(64, 64, out));
console.log(`done: ${OUT}`);
console.log(`headPixels=${headPixels} shellPixels=${shellPixels} transparent=${64 * 64 - headPixels - shellPixels}`);

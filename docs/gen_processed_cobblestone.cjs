// 生成 processed_soft_cobblestone 纹理：圆石底材 + 裂纹石砖的黑色裂纹。
// 用法：node docs/gen_processed_cobblestone.cjs
// 依赖：Node 内置 zlib（含 zlib.crc32，Node 22+）。
const fs = require('fs');
const zlib = require('zlib');
const path = require('path');

const SRC_COBBLE = path.join(__dirname, '..', '.tmp-tex', 'assets', 'minecraft', 'textures', 'block', 'cobblestone.png');
const SRC_CRACKED = path.join(__dirname, '..', '.tmp-tex', 'assets', 'minecraft', 'textures', 'block', 'cracked_stone_bricks.png');
const OUT = path.join(__dirname, '..', 'src', 'main', 'resources', 'assets', 'blockrooms', 'textures', 'block', 'processed_soft_cobblestone.png');

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
        const p = palette[idx];
        if (!p) throw new Error('palette index out of range');
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

const cobble = decodePng(fs.readFileSync(SRC_COBBLE));
const cracked = decodePng(fs.readFileSync(SRC_CRACKED));
if (cobble.width !== cracked.width || cobble.height !== cracked.height) {
  throw new Error('size mismatch');
}

// 裂纹提取：裂纹石砖中亮度 < THRESHOLD 的像素视为裂纹（原版裂纹约为 #5a-#6e 深灰）。
// 软过渡：T-15..T 之间按比例混合，避免硬切锯齿；裂纹色再压暗 DARKEN 倍呈现"黑色裂纹"。
const T = 100;
const DARKEN = 1;
let crackPixels = 0;
const out = Buffer.alloc(cobble.rgba.length);
for (let i = 0; i < cobble.rgba.length; i += 4) {
  const cr = cracked.rgba[i], cg = cracked.rgba[i + 1], cb = cracked.rgba[i + 2];
  const l = lum(cr, cg, cb);
  const ca = cracked.rgba[i + 3];
  let r = cobble.rgba[i], g = cobble.rgba[i + 1], b = cobble.rgba[i + 2];
  const a = cobble.rgba[i + 3];
  if (ca > 0 && l < T) {
    const t = l < T - 15 ? 1 : (T - l) / 15; // 1=完全裂纹色, 0=圆石
    r = Math.round(r * (1 - t) + cr * DARKEN * t);
    g = Math.round(g * (1 - t) + cg * DARKEN * t);
    b = Math.round(b * (1 - t) + cb * DARKEN * t);
    crackPixels++;
  }
  out[i] = r; out[i + 1] = g; out[i + 2] = b; out[i + 3] = a;
}

fs.writeFileSync(OUT, encodePng(cobble.width, cobble.height, out));
console.log(`done: ${OUT}`);
console.log(`size=${cobble.width}x${cobble.height} crackPixels=${crackPixels}/${cobble.rgba.length / 4}`);

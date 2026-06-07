export const fallbackImages = [
  'https://picsum.photos/seed/maikaitui-lake/900/620',
  'https://picsum.photos/seed/maikaitui-mountain/900/620',
  '/images/detail-zhangjiajie.png',
  'https://picsum.photos/seed/maikaitui-river/900/620',
  'https://picsum.photos/seed/maikaitui-village/900/620'
]

export const fallbackAttractions = [
  {
    id: 1,
    name: '九寨沟风景区',
    regionName: '四川 · 阿坝',
    categoryName: '自然风光',
    rating: 4.8,
    price: 248,
    coverImage: fallbackImages[0],
    description: '湖泊、瀑布、彩林与雪峰交织，是适合慢旅行和自然探索的代表目的地。',
    address: '四川省阿坝藏族羌族自治州九寨沟县',
    openTime: '07:30 - 17:00',
    viewCount: 32800,
    likeCount: 1280
  },
  {
    id: 2,
    name: '黄山风景区',
    regionName: '安徽 · 黄山',
    categoryName: '山水名胜',
    rating: 4.7,
    price: 190,
    coverImage: fallbackImages[1],
    description: '以奇松、怪石、云海、温泉闻名，适合摄影、徒步和短途度假。',
    address: '安徽省黄山市黄山区汤口镇',
    openTime: '06:20 - 17:30',
    viewCount: 28600,
    likeCount: 960
  },
  {
    id: 3,
    name: '张家界国家森林公园',
    regionName: '湖南 · 张家界',
    categoryName: '世界自然遗产',
    rating: 4.6,
    price: 248,
    coverImage: fallbackImages[2],
    description: '峰林地貌层峦叠翠，云雾缭绕，是自然观光与户外体验的经典目的地。',
    address: '湖南省张家界市武陵源区',
    openTime: '07:00 - 18:00',
    viewCount: 32500,
    likeCount: 1120
  },
  {
    id: 4,
    name: '桂林漓江风景区',
    regionName: '广西 · 桂林',
    categoryName: '自然风光',
    rating: 4.7,
    price: 210,
    coverImage: fallbackImages[3],
    description: '山水相映，舟行画中，是亲子游、摄影游和轻度假线路的高频选择。',
    address: '广西壮族自治区桂林市',
    openTime: '08:00 - 17:30',
    viewCount: 22100,
    likeCount: 820
  },
  {
    id: 5,
    name: '西湖风景区',
    regionName: '浙江 · 杭州',
    categoryName: '城市观光',
    rating: 4.6,
    price: 0,
    coverImage: fallbackImages[4],
    description: '湖山相依，人文底蕴丰厚，适合城市漫游、亲子散步和周末短途。',
    address: '浙江省杭州市西湖区龙井路1号',
    openTime: '全天开放',
    viewCount: 36400,
    likeCount: 1450
  }
]

export const fallbackCategories = [
  { id: 1, name: '自然风光', icon: 'mountain', desc: '山川湖海' },
  { id: 2, name: '历史人文', icon: 'temple', desc: '古迹文化' },
  { id: 3, name: '城市观光', icon: 'building', desc: '都市风情' },
  { id: 4, name: '休闲度假', icon: 'umbrella', desc: '放松身心' },
  { id: 5, name: '亲子游', icon: 'family', desc: '亲子时光' },
  { id: 6, name: '户外探险', icon: 'trail', desc: '挑战自我' }
]

const regionMap = {
  2: '北京',
  3: '上海',
  4: '浙江',
  5: '浙江 · 杭州',
  6: '浙江 · 杭州',
  7: '四川',
  8: '四川 · 成都',
  9: '云南',
  10: '云南 · 大理',
  11: '海南',
  12: '海南 · 三亚'
}

const categoryMap = {
  1: '自然风光',
  2: '历史人文',
  3: '主题乐园',
  4: '博物展馆',
  5: '美食街区',
  6: '自然公园',
  7: '山水名胜'
}

export function flattenTree(nodes, out = []) {
  if (!Array.isArray(nodes)) return out
  nodes.forEach((node) => {
    out.push({ ...node, children: undefined })
    if (node.children?.length) flattenTree(node.children, out)
  })
  return out
}

export function parseImages(images, coverImage, seed = 'maikaitui') {
  let list = []
  if (Array.isArray(images)) {
    list = images
  } else if (typeof images === 'string' && images.trim()) {
    try {
      const parsed = JSON.parse(images)
      list = Array.isArray(parsed) ? parsed : []
    } catch {
      list = images.split(',').map((item) => item.trim()).filter(Boolean)
    }
  }

  if (coverImage) list.unshift(coverImage)
  const unique = [...new Set(list.filter(Boolean))]
  if (!unique.length) {
    unique.push(`https://picsum.photos/seed/${encodeURIComponent(String(seed))}/900/620`)
  }
  while (unique.length < 8) {
    unique.push(`https://picsum.photos/seed/${encodeURIComponent(String(seed))}-gallery-${unique.length}/900/620`)
  }
  return unique
}

export function normalizeAttraction(item = {}, index = 0) {
  const seed = item.id || item.name || `fallback-${index}`
  const images = parseImages(item.images, item.coverImage, seed)
  return {
    ...item,
    name: item.name || '未命名景点',
    regionName: item.regionName || regionMap[item.regionId] || item.address?.split('，')?.[0] || '热门目的地',
    categoryName: item.categoryName || categoryMap[item.categoryId] || '精选景点',
    rating: Number(item.rating ?? 4.6),
    price: Number(item.price ?? 0),
    coverImage: images[0],
    images,
    viewCount: Number(item.viewCount ?? 0),
    likeCount: Number(item.likeCount ?? item.favoriteCount ?? 0)
  }
}

export function normalizeAttractionList(data, fallback = fallbackAttractions) {
  const rows = Array.isArray(data) ? data : (data?.records || data?.list || [])
  const source = rows.length ? rows : fallback
  return source.map(normalizeAttraction)
}

export function getPageRows(data) {
  const rows = Array.isArray(data) ? data : (data?.records || data?.list || [])
  const total = Array.isArray(data) ? data.length : (data?.total ?? data?.totalCount ?? rows.length)
  return { rows, total }
}

export function formatPrice(price) {
  const numeric = Number(price || 0)
  return numeric > 0 ? `¥${numeric}` : '免费'
}

export function formatCount(count) {
  const numeric = Number(count || 0)
  if (numeric >= 10000) return `${(numeric / 10000).toFixed(1)}万`
  if (numeric >= 1000) return `${(numeric / 1000).toFixed(1)}千`
  return String(numeric)
}

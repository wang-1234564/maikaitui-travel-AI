export const fallbackImages = [
  'https://picsum.photos/seed/mini-lake/700/480',
  'https://picsum.photos/seed/mini-mountain/700/480',
  '/static/images/detail-zhangjiajie.png',
  'https://picsum.photos/seed/mini-river/700/480'
]

export const fallbackAttractions = [
  { id: 1, name: '九寨沟风景区', regionName: '四川 · 阿坝', categoryName: '自然风光', rating: 4.8, price: 248, coverImage: fallbackImages[0], viewCount: 32000, address: '四川省阿坝州九寨沟县', openTime: '07:30 - 17:00', description: '湖泊、瀑布、彩林与雪峰交织，是适合慢旅行和自然探索的代表目的地。' },
  { id: 2, name: '黄山风景区', regionName: '安徽 · 黄山', categoryName: '山水名胜', rating: 4.7, price: 190, coverImage: fallbackImages[1], viewCount: 28600, address: '安徽省黄山市黄山区', openTime: '06:20 - 17:30', description: '以奇松、怪石、云海、温泉闻名，适合摄影、徒步和短途度假。' },
  { id: 3, name: '张家界国家森林公园', regionName: '湖南 · 张家界', categoryName: '世界自然遗产', rating: 4.6, price: 248, coverImage: fallbackImages[2], viewCount: 32500, address: '湖南省张家界市武陵源区', openTime: '07:00 - 18:00', description: '峰林地貌层峦叠翠，云雾缭绕，是自然观光与户外体验的经典目的地。' },
  { id: 4, name: '桂林漓江风景区', regionName: '广西 · 桂林', categoryName: '自然风光', rating: 4.7, price: 210, coverImage: fallbackImages[3], viewCount: 22100, address: '广西桂林市', openTime: '08:00 - 17:30', description: '山水相映，舟行画中，是亲子游、摄影游和轻度假线路的高频选择。' }
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

export const fallbackCategories = [
  { id: 1, name: '找景区', iconKind: 'mountain', iconImage: '/static/icons/home/find-area.png' },
  { id: 2, name: '找景点', iconKind: 'pin', iconImage: '/static/icons/home/find-spot.png' },
  { id: 3, name: '找攻略', iconKind: 'guide', iconImage: '/static/icons/home/find-guide.png' },
  { id: 4, name: 'AI对话', iconKind: 'ai', iconImage: '/static/icons/home/ai-chat.png' },
  { id: 5, name: '自然风光', iconKind: 'nature', iconImage: '/static/icons/home/nature.png' },
  { id: 6, name: '历史人文', iconKind: 'culture', iconImage: '/static/icons/home/culture.png' },
  { id: 7, name: '城市观光', iconKind: 'city', iconImage: '/static/icons/home/city.png' },
  { id: 8, name: '全部分类', iconKind: 'grid', iconImage: '/static/icons/home/all-category.png' }
]

export function flattenTree(nodes, out = []) {
  if (!Array.isArray(nodes)) return out
  nodes.forEach((node) => {
    out.push({ ...node, children: undefined })
    if (node.children?.length) flattenTree(node.children, out)
  })
  return out
}

export function parseImages(images, coverImage, seed = 'mini') {
  let list = []
  if (Array.isArray(images)) list = images
  else if (typeof images === 'string' && images.trim()) {
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
    unique.push(`https://picsum.photos/seed/${encodeURIComponent(String(seed))}/700/480`)
  }
  while (unique.length < 5) {
    unique.push(`https://picsum.photos/seed/${encodeURIComponent(String(seed))}-gallery-${unique.length}/700/480`)
  }
  return unique
}

export function normalizeAttraction(item = {}, index = 0) {
  const seed = item.id || item.name || index
  const images = parseImages(item.images, item.coverImage, seed)
  return {
    ...item,
    name: item.name || '未命名景区',
    coverImage: images[0],
    images,
    regionName: item.regionName || regionMap[item.regionId] || '热门目的地',
    categoryName: item.categoryName || categoryMap[item.categoryId] || '精选景区',
    rating: Number(item.rating ?? 4.6),
    price: Number(item.price ?? 0),
    viewCount: Number(item.viewCount ?? 0)
  }
}

export function normalizeList(data, fallback = fallbackAttractions) {
  const rows = Array.isArray(data) ? data : (data?.records || data?.list || [])
  const source = rows.length ? rows : fallback
  return source.map(normalizeAttraction)
}

export function formatPrice(price) {
  const n = Number(price || 0)
  return n > 0 ? `¥${n}` : '免费'
}

export function formatCount(count) {
  const n = Number(count || 0)
  if (n >= 10000) return `${(n / 10000).toFixed(1)}万`
  return String(n)
}

<template>
  <div class="map-nav-page">
    <!-- 地图 — 铺满全屏 -->
    <div id="nav-map" class="map-container"></div>

    <!-- 返回按钮 — 悬浮左上角 -->
    <button class="back-float" @click="$router.back()">
      <svg
        width="22"
        height="22"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2.5"
      >
        <path d="M15 18l-6-6 6-6" />
      </svg>
    </button>

    <!-- 定位按钮 — 悬浮右下 -->
    <button class="locate-float" @click="centerOnUser">
      <svg
        width="20"
        height="20"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
      >
        <circle cx="12" cy="12" r="3" />
        <path d="M12 2v4M12 18v4M2 12h4M18 12h4" />
      </svg>
    </button>

    <!-- 底部信息卡 — 悬浮在地图上 -->
    <div class="info-overlay">
      <div class="info-dest">
        <span class="dest-name">{{ attraction?.name || "景点" }}</span>
        <span class="dest-addr" v-if="attraction?.address">{{
          attraction.address
        }}</span>
      </div>
      <div class="info-dist">
        <div class="dist-item">
          <span class="dist-num">{{ distanceText }}</span>
          <span class="dist-label">直线距离</span>
        </div>
        <div class="dist-item">
          <span class="dist-num">{{ walkTime }}</span>
          <span class="dist-label">🚶 步行</span>
        </div>
        <div class="dist-item">
          <span class="dist-num">{{ driveTime }}</span>
          <span class="dist-label">🚗 驾车</span>
        </div>
      </div>
      <button class="nav-btn" @click="openExternalNav">
        打开地图 APP 导航
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from "vue";
import { useRoute } from "vue-router";
import { getAttractionById } from "@/api";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

const route = useRoute();
const attraction = ref(null);
const distanceText = ref("--");
const walkTime = ref("--");
const driveTime = ref("--");

let map = null;
let userMarker = null;
let attractionMarker = null;
let routeLine = null;
let userLatLng = null;
let watchId = null;

// 修复 Leaflet 图标
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl:
    "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
  iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
  shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
});

const userIcon = L.divIcon({
  className: "custom-user-marker",
  html: '<div style="width:16px;height:16px;background:#4285f4;border:3px solid #fff;border-radius:50%;box-shadow:0 2px 8px rgba(0,0,0,.35)"></div>',
  iconSize: [22, 22],
  iconAnchor: [11, 11],
});

const attractionIcon = L.divIcon({
  className: "custom-attraction-marker",
  html: '<div style="width:32px;height:32px;background:#ea4335;border:3px solid #fff;border-radius:50% 50% 50% 0;transform:rotate(-45deg);box-shadow:0 3px 10px rgba(0,0,0,.35)"></div>',
  iconSize: [38, 38],
  iconAnchor: [19, 34],
});

function toRad(d) {
  return (d * Math.PI) / 180;
}

function haversine(lat1, lon1, lat2, lon2) {
  const R = 6371;
  const dLat = toRad(lat2 - lat1),
    dLon = toRad(lon2 - lon1);
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) ** 2;
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

function updateStats() {
  if (!userLatLng || !attraction.value?.latitude) return;
  const d = haversine(
    userLatLng.lat,
    userLatLng.lng,
    attraction.value.latitude,
    attraction.value.longitude,
  );
  distanceText.value =
    d < 1 ? `${(d * 1000).toFixed(0)} m` : `${d.toFixed(1)} km`;
  walkTime.value =
    d < 1
      ? `${Math.round((d * 1000) / 80)}分钟`
      : `${Math.round((d / 5) * 60)}分钟`;
  driveTime.value =
    d < 1
      ? `${Math.round((d * 1000) / 500)}分钟`
      : `${Math.round((d / 40) * 60)}分钟`;
}

function drawRoute() {
  if (!userLatLng || !attraction.value?.latitude) return;
  if (routeLine) map.removeLayer(routeLine);
  routeLine = L.polyline(
    [
      [userLatLng.lat, userLatLng.lng],
      [attraction.value.latitude, attraction.value.longitude],
    ],
    { color: "#4285f4", weight: 4, dashArray: "10 6", opacity: 0.7 },
  ).addTo(map);
  map.fitBounds(routeLine.getBounds().pad(0.25));
}

function centerOnUser() {
  if (userLatLng)
    map.setView([userLatLng.lat, userLatLng.lng], 15, { animate: true });
}

function initMap(lat, lng) {
  map = L.map("nav-map", {
    center: [lat, lng],
    zoom: 14,
    zoomControl: true,
    attributionControl: false,
  });
  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
  }).addTo(map);
  // 放置景点标记
  if (attraction.value?.latitude) {
    attractionMarker = L.marker(
      [attraction.value.latitude, attraction.value.longitude],
      { icon: attractionIcon },
    )
      .addTo(map)
      .bindPopup(
        `<b>${attraction.value.name || "景点"}</b><br>${attraction.value.address || ""}`,
      );
  }
}

function onLocation(pos) {
  userLatLng = { lat: pos.coords.latitude, lng: pos.coords.longitude };
  if (!map) {
    initMap(
      attraction.value?.latitude || userLatLng.lat,
      attraction.value?.longitude || userLatLng.lng,
    );
  }
  if (userMarker) map.removeLayer(userMarker);
  userMarker = L.marker([userLatLng.lat, userLatLng.lng], {
    icon: userIcon,
  }).addTo(map);
  updateStats();
  drawRoute();
}

function onLocationErr() {
  if (!map && attraction.value?.latitude)
    initMap(attraction.value.latitude, attraction.value.longitude);
  distanceText.value = "需定位权限";
}

function openExternalNav() {
  const a = attraction.value;
  if (!a?.latitude) return;
  const name = encodeURIComponent(a.name || "景点");
  const ua = navigator.userAgent.toLowerCase();
  if (ua.includes("micromessenger")) {
    window.open(
      `https://apis.map.qq.com/uri/v1/marker?marker=coord:${a.latitude},${a.longitude};title:${name}&referer=maikaitui`,
    );
  } else {
    window.open(
      `https://uri.amap.com/marker?position=${a.longitude},${a.latitude}&name=${name}`,
    );
  }
}

onMounted(async () => {
  try {
    attraction.value = await getAttractionById(route.params.id);
  } catch {
    /* */
  }
  if ("geolocation" in navigator) {
    watchId = navigator.geolocation.watchPosition(onLocation, onLocationErr, {
      enableHighAccuracy: true,
      timeout: 15000,
      maximumAge: 60000,
    });
  } else {
    onLocationErr();
  }
});

onBeforeUnmount(() => {
  if (watchId) navigator.geolocation.clearWatch(watchId);
  if (map) {
    map.remove();
    map = null;
  }
});
</script>

<style scoped>
.map-nav-page {
  position: fixed;
  inset: 0;
  width: 100vw;
  height: 100vh;
  z-index: 100;
  background: #edf1f5;
  padding: 12px;
  box-sizing: border-box;
}

.map-container {
  width: 100%;
  height: 100%;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.08);
}

/* 返回按钮 */
.back-float {
  position: fixed;
  top: 100px;
  left: 28px;
  z-index: 1000;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.16);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #333;
}

/* 定位按钮 */
.locate-float {
  position: fixed;
  right: 28px;
  bottom: 300px;
  z-index: 1000;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.16);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4285f4;
}

/* 底部卡片 */
.info-overlay {
  position: fixed;
  bottom: 28px;
  left: 28px;
  right: 28px;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.97);
  backdrop-filter: blur(16px);
  border-radius: 18px;
  padding: 18px 20px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.1);
}

.info-dest {
  margin-bottom: 14px;
}

.dest-name {
  display: block;
  font-size: 17px;
  font-weight: 700;
  color: #222;
}

.dest-addr {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.info-dist {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.dist-item {
  flex: 1;
  text-align: center;
  background: #f5f7fa;
  border-radius: 12px;
  padding: 12px 6px;
}

.dist-num {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

.dist-label {
  display: block;
  font-size: 11px;
  color: #999;
  margin-top: 3px;
}

.nav-btn {
  width: 100%;
  height: 46px;
  border-radius: 12px;
  background: linear-gradient(135deg, #4285f4, #1a73e8);
  color: #fff;
  font-weight: 700;
  font-size: 15px;
}
</style>

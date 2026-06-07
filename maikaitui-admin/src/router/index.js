import { createRouter, createWebHistory } from "vue-router";

import Layout from "@/components/layout/Layout.vue";

const routes = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/login/Login.vue"),
    meta: { title: "登录", requiresAuth: false },
  },
  {
    path: "/",
    name: "Layout",
    component: Layout,
    redirect: "/dashboard",
    hidden: true,
    meta: { requiresAuth: true },
    children: [
      {
        path: "dashboard",
        name: "Dashboard",
        component: () => import("@/views/dashboard/Dashboard.vue"),
        meta: { title: "首页", icon: "HomeFilled" },
      },
    ],
  },
  {
    path: "/system",
    name: "SystemLayout",
    component: Layout,
    redirect: "/system/user",
    meta: { title: "系统管理", icon: "Setting", requiresAuth: true },
    children: [
      {
        path: "user",
        name: "UserManagement",
        component: () => import("@/views/system/user/UserManagement.vue"),
        meta: { title: "用户管理", icon: "User" },
      },
      {
        path: "role",
        name: "RoleManagement",
        component: () => import("@/views/system/role/RoleManagement.vue"),
        meta: { title: "角色管理", icon: "Avatar" },
      },
      {
        path: "menu",
        name: "MenuManagement",
        component: () => import("@/views/system/menu/MenuManagement.vue"),
        meta: { title: "菜单管理", icon: "Menu" },
      },
      {
        path: "dict",
        name: "DictManagement",
        component: () => import("@/views/system/dict/DictManagement.vue"),
        meta: { title: "字典管理", icon: "Collection" },
      },
      {
        path: "log",
        name: "LogManagement",
        component: () => import("@/views/system/log/LogManagement.vue"),
        meta: { title: "操作日志", icon: "Document" },
      },
    ],
  },
  {
    path: "/tourism",
    name: "TourismLayout",
    component: Layout,
    redirect: "/tourism/attraction",
    meta: { title: "旅游管理", icon: "Place", requiresAuth: true },
    children: [
      {
        path: "attraction",
        name: "AttractionManagement",
        component: () =>
          import("@/views/tourism/attraction/AttractionManagement.vue"),
        meta: { title: "景点管理", icon: "PictureFilled" },
      },
      {
        path: "region",
        name: "RegionManagement",
        component: () => import("@/views/tourism/region/RegionManagement.vue"),
        meta: { title: "地区管理", icon: "Location" },
      },
      {
        path: "category",
        name: "CategoryManagement",
        component: () =>
          import("@/views/tourism/category/CategoryManagement.vue"),
        meta: { title: "分类管理", icon: "Grid" },
      },
      {
        path: "order",
        name: "OrderManagement",
        component: () => import("@/views/tourism/order/OrderManagement.vue"),
        meta: { title: "订单管理", icon: "Tickets" },
      },
      {
        path: "comment",
        name: "CommentManagement",
        component: () =>
          import("@/views/tourism/comment/CommentManagement.vue"),
        meta: { title: "评论管理", icon: "ChatDotRound" },
      },
    ],
  },
  {
    path: "/ai",
    name: "AiLayout",
    component: Layout,
    redirect: "/ai/chat",
    meta: { title: "AI智能", icon: "Cpu", requiresAuth: true },
    children: [
      {
        path: "chat",
        name: "AiChat",
        component: () => import("@/views/ai/AiChat.vue"),
        meta: { title: "AI控制台", icon: "Cpu" },
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
});

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem("token");

  if (to.meta.requiresAuth !== false) {
    if (!token) {
      next({ name: "Login", query: { redirect: to.fullPath } });
      return;
    }
  }

  if (to.name === "Login" && token) {
    next({ name: "Dashboard" });
    return;
  }

  document.title = to.meta.title
    ? `${to.meta.title} - 迈开腿后台管理`
    : "迈开腿 - 后台管理系统";
  next();
});

export default router;

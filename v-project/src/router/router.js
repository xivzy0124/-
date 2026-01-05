import { createRouter, createWebHashHistory } from "vue-router";

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: "/",
      component: () => import("/src/pages/Dashboard.vue"),
    },
    {
      path: "/dashboard",
      component: () => import("/src/pages/Dashboard.vue"),
    },
  ],
});

export default router;

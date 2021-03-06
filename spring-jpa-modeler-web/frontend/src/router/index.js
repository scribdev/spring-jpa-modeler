import Vue from 'vue';
import VueRouter from 'vue-router';
import LoginPage from '@/views/LoginPage.vue';
import SignUpPage from '@/views/SignUpPage.vue';

Vue.use(VueRouter);

const routes = [{
  path: '/login',
  name: 'LoginPage',
  component: LoginPage,
}, {
  path: '/',
  name: 'SignUpPage',
  component: SignUpPage,
}];

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes,
});

export default router;

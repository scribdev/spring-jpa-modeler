import { shallowMount } from '@vue/test-utils';
import LoginPage from '@/views/LoginPage.vue';
import Vue from 'vue';
import Vuetify from 'vuetify';

Vue.use(Vuetify);

describe('LoginPage.vue', () => {
  it('renders props.msg when passed', () => {
    const msg = 'Login';
    const wrapper = shallowMount(LoginPage, {
      propsData: { msg },
    });
    expect(wrapper.text()).toMatch(msg);
  });
});

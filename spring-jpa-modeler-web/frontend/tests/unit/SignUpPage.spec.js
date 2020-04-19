import { mount, createLocalVue } from '@vue/test-utils';
import Vue from 'vue';
import Vuetify from 'vuetify';
import SignUpPage from '@/views/SignUpPage.vue';

Vue.use(Vuetify);

const localVue = createLocalVue();

describe('SignUpPage.vue', () => {
  let vuetify;

  beforeEach(() => {
    vuetify = new Vuetify();
  });

  it('renders props.msg when passed', () => {
    const wrapper = mount(SignUpPage, {
      localVue,
      vuetify,
    });

    expect(wrapper.get('#username').element.value).toEqual('');
  });
});

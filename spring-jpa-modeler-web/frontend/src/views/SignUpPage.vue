<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="4">
        <v-overlay opacity="0.5" color="white">
          <v-card class="elevation-12" min-width="400" light>
            <v-toolbar color="amber">
              <v-toolbar-title>
                <span class="title ml-3 mr-5">
                  Spring JPA&nbsp;
                  <span class="font-weight-light">Modeller</span>
                </span>
              </v-toolbar-title>
              <v-spacer />
            </v-toolbar>
            <v-card-text>
              <v-form>
                <v-text-field
                  id="username"
                  label="Username"
                  name="username"
                  prepend-icon="mdi-account"
                  type="text"
                />
                <v-text-field label="Email" name="email" prepend-icon="mdi-email" type="text" />
                <v-text-field
                  id="password"
                  label="Password"
                  name="password"
                  prepend-icon="mdi-key"
                  type="password"
                />
                <v-text-field
                  id="confirmPassword"
                  label="Confirm Password"
                  name="confirmPassword"
                  prepend-icon="mdi-key"
                  type="password"
                />
              </v-form>
            </v-card-text>
            <v-card-actions>
              <v-spacer />
              <v-btn color="primary">Sign Up</v-btn>
            </v-card-actions>
          </v-card>
        </v-overlay>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
export default {
};
</script>

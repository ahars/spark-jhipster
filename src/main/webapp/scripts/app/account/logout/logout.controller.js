'use strict';

angular.module('sparkJhipsterApp')
    .controller('LogoutController', function (Auth) {
        Auth.logout();
    });

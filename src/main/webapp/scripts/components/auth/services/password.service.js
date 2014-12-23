'use strict';

angular.module('sparkJhipsterApp')
    .factory('Password', function ($resource) {
        return $resource('api/account/change_password', {}, {
        });
    });

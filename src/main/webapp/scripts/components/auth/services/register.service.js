'use strict';

angular.module('sparkJhipsterApp')
    .factory('Register', function ($resource) {
        return $resource('api/register', {}, {
        });
    });



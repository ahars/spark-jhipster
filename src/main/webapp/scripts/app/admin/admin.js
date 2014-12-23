'use strict';

angular.module('sparkJhipsterApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('admin', {
                abstract: true,
                parent: 'site'
            });
    });

'use strict';

angular.module('sparkJhipsterApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('metrics', {
                parent: 'admin',
                url: '/metrics',
                data: {
                    roles: ['ROLE_ADMIN']
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/admin/metrics/metrics.html',
                        controller: 'MetricsController'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('metrics');
                        return $translate.refresh();
                    }]
                }
            });
    });

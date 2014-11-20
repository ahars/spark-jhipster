'use strict';

jhipsterSandboxApp
    .config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
            $routeProvider
                .when('/author', {
                    templateUrl: 'views/authors.html',
                    controller: 'AuthorController',
                    resolve:{
                        resolvedAuthor: ['Author', function (Author) {
                            return Author.query().$promise;
                        }],
                        resolvedBook: ['Book', function (Book) {
                            return Book.query().$promise;
                        }]
                    },
                    access: {
                        authorizedRoles: [USER_ROLES.all]
                    }
                })
        });

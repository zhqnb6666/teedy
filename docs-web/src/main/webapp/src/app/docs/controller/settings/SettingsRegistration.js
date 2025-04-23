'use strict';

/**
 * Settings registration controller.
 */
angular.module('docs').controller('SettingsRegistration', function($scope, Restangular, $dialog, $translate) {
  /**
   * Load registrations from server.
   */
  $scope.loadRegistrations = function() {
    Restangular.one('user/registration').get().then(function(data) {
      $scope.registrations = data.registrations;
    });
  };

  // Load registrations initially
  $scope.loadRegistrations();

  /**
   * Approve a registration request.
   * 
   * @param registration Registration to approve
   */
  $scope.approve = function(registration) {
    var title = $translate.instant('settings.registration.approve_title');
    var msg = $translate.instant('settings.registration.approve_message', { username: registration.username });
    var btns = [
      {result: 'cancel', label: $translate.instant('cancel')},
      {result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}
    ];

    $dialog.messageBox(title, msg, btns).open().then(function(result) {
      if (result === 'ok') {
        Restangular.one('user/registration', registration.id).post('status', {
          approve: true
        }).then(function() {
          $scope.loadRegistrations();
        });
      }
    });
  };

  /**
   * Reject a registration request.
   * 
   * @param registration Registration to reject
   */
  $scope.reject = function(registration) {
    var title = $translate.instant('settings.registration.reject_title');
    var msg = $translate.instant('settings.registration.reject_message', { username: registration.username });
    var btns = [
      {result: 'cancel', label: $translate.instant('cancel')},
      {result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}
    ];

    $dialog.messageBox(title, msg, btns).open().then(function(result) {
      if (result === 'ok') {
        Restangular.one('user/registration', registration.id).post('status', {
          approve: false
        }).then(function() {
          $scope.loadRegistrations();
        });
      }
    });
  };
}); 
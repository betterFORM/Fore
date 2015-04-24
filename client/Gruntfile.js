'use strict';
var LIVERELOAD_PORT = 35729;
var lrSnippet = require('connect-livereload')({port: LIVERELOAD_PORT});
var mountFolder = function (connect, dir) {
    return connect.static(require('path').resolve(dir));
};

module.exports = function(grunt) {
    // load all grunt tasks
    require('matchdep').filterDev('grunt-*').forEach(grunt.loadNpmTasks);
    require('time-grunt')(grunt);

    var httpServerPort = 9001;

    grunt.initConfig({
        vars: grunt.file.readJSON('package.json'),

        mkdir: {
            pages: {
                options: {
                    mode: '0700',
                    create: ['<%= vars.webTarget %>/pages/']
                }
            }
        },
        //WATCH tasks
        watch: {
            options: {
                nospawn: true,
                livereload: true
            },
            pages:{
                files: ['pages/**','custom-elements/**'],
                tasks: ['mkdir:pages','rsync:pages']
            },
            target: {
                options: {
                    livereload: true
                },
                files: ['**/*.html']
            }
        },

        connect: {
            //Run "app" in grunt server
            livereload: {
                options: {
                    port: httpServerPort,
                    base:  '.',
                    keepalive:false,
                    open: true,
                    livereload: true
                }
            }
        },

        rsync: {
            options: {
                args: ["-vpc"],
                recursive: true
            },
            pages:{
                options: {
                    src: ['pages/**'],
                    dest: '<%= vars.webTarget %>/pages/'
                }
            },
            bower:{
                options: {
                    src: ['bower_components/**'],
                    dest: '<%= vars.webTarget %>/bower_components/'
                }
            },
            distributeElements: {
                options: {
                    src: 'custom-elements/**',
                    dest: '<%= vars.webTarget %>/custom-elements/'
                }
            }
        }
    });

    /*
     This task must be used when deploying dev version of fore into ../web/target
     */
    grunt.registerTask('build-xar', [
        'zip'
    ]);


    /*
    This task must be used when deploying dev version of fore into ../web/target
    */
    grunt.registerTask('deploy', [
       'mkdir:pages',
       'rsync'
    ]);

    /*
    run a local server without connection to betterFORM webapp.
    */
    grunt.registerTask('server',  [
        'connect:livereload',
        'watch'
    ]);
};


// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.

const gulp = require('gulp');
const cp = require('child_process');
const path = require('path');
const fs = require('fs');
const glob = require('glob');
const rimraf = require("rimraf");

const rootPath = path.join(__dirname, '..');

gulp.task('clean', (done) => {
    rimraf.sync(path.join(__dirname, 'repository'));
    rimraf.sync(path.join(__dirname, 'workspace'));
    const packages = glob.sync('*.tgz');
    for (const package of packages) {
        fs.unlinkSync(package);
    }
    done();
});

gulp.task('build-indexer', (done) => {
    cp.execSync(`${mvnw()} clean verify`, { cwd: rootPath, stdio: [0, 1, 2] });
    const srouceRepositoryPath = path.join(rootPath, 'com.microsoft.java.lsif.product', 'target', 'repository');
    const targetRepositoryPath = path.join(rootPath, 'cmd', 'repository');

    gulp.src(path.join(srouceRepositoryPath, 'config_linux', '**/*')).pipe(gulp.dest(path.join(targetRepositoryPath, 'config_linux')));
    gulp.src(path.join(srouceRepositoryPath, 'config_mac', '**/*')).pipe(gulp.dest(path.join(targetRepositoryPath, 'config_mac')));
    gulp.src(path.join(srouceRepositoryPath, 'config_win', '**/*')).pipe(gulp.dest(path.join(targetRepositoryPath, 'config_win')));
    gulp.src(path.join(srouceRepositoryPath, 'features', '**/*')).pipe(gulp.dest(path.join(targetRepositoryPath, 'features')));
    gulp.src(path.join(srouceRepositoryPath, 'plugins', '**/*')).pipe(gulp.dest(path.join(targetRepositoryPath, 'plugins')));
    done();
});

gulp.task('build', gulp.series('clean', 'build-indexer'));

function isWin() {
    return /^win/.test(process.platform);
}

function mvnw() {
    return isWin() ? 'mvnw.cmd' : './mvnw';
}
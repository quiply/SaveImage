var ImageSaver = function () {
};

ImageSaver.saveImageToGallery = function (localImagePath, successCallback, failureCallback) {
    if (typeof successCallback != 'function') {
        throw new Error('SaveImage Error: successCallback is not a function');
    }

    if (typeof failureCallback != 'function') {
        throw new Error('SaveImage Error: failureCallback is not a function');
    }

    return cordova.exec(
        successCallback, failureCallback, 'SaveImage', 'saveImageToGallery', [_getLocalImagePathWithoutPrefix()]);

    function _getLocalImagePathWithoutPrefix() {
        if (localImagePath.indexOf('file:///') === 0) {
            return localImagePath.substring(7);
        }
        return localImagePath;
    }
};

module.exports = ImageSaver;

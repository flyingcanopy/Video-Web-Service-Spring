/*
 *
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.magnum.dataup;

import org.apache.http.HttpResponse;
import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Controller
public class VideoController {

    private Map<Long, Video> videos;
    private static final AtomicLong currentId = new AtomicLong(0L);

    public VideoController() {
        videos = new HashMap<>();
    }

    /**
     * You will need to create one or more Spring controllers to fulfill the
     * requirements of the assignment. If you use this file, please rename it
     * to something other than "AnEmptyController"
     * <p>
     * <p>
     * ________  ________  ________  ________          ___       ___  ___  ________  ___  __
     * |\   ____\|\   __  \|\   __  \|\   ___ \        |\  \     |\  \|\  \|\   ____\|\  \|\  \
     * \ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \       \ \  \    \ \  \\\  \ \  \___|\ \  \/  /|_
     * \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \       \ \  \    \ \  \\\  \ \  \    \ \   ___  \
     * \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \       \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \
     * \ \_______\ \_______\ \_______\ \_______\       \ \_______\ \_______\ \_______\ \__\\ \__\
     * \|_______|\|_______|\|_______|\|_______|        \|_______|\|_______|\|_______|\|__| \|__|
     */

    public Video save(Video entity) {
        checkAndSetId(entity);
        videos.put(entity.getId(), entity);
        return entity;
    }

    private void checkAndSetId(Video entity) {
        if (entity.getId() == 0) {
            entity.setId(currentId.incrementAndGet());
        }
    }

    private String getDataUrl(long videoId) {
        String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
        return url;
    }

    private String getUrlBaseForLocalServer() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String base =
                "http://" + request.getServerName()
                        + ((request.getServerPort() != 80) ? ":" + request.getServerPort() : "");
        return base;
    }

    @RequestMapping(value = "/video", method = RequestMethod.GET)
    public @ResponseBody
    Collection<Video> getVideos() {
        return videos.values();
    }

    @RequestMapping(value = "/video", method = RequestMethod.POST)
    public @ResponseBody
    Video
    getVideo(@RequestBody Video video) {
        save(video);
        video.setDataUrl(getDataUrl(video.getId()));
        return video;
    }

    @RequestMapping(value = "/video/{id}/data", method = RequestMethod.POST)
    public @ResponseBody
    VideoStatus
    saveVideo(@PathVariable("id") long videoToken, @RequestParam("data") MultipartFile video) throws IOException {

        VideoStatus videoStatus = null;
        if (videos.containsKey(videoToken)) {
            VideoFileManager videoFileManager = VideoFileManager.get();
            videoFileManager.saveVideoData(videos.get(videoToken), video.getInputStream());
            videoStatus = new VideoStatus(VideoStatus.VideoState.READY);
        } else {
            throw new ResourceNotFoundException("404");
        }
        return videoStatus;

    }

    @RequestMapping(value = "/video/{id}/data", method = RequestMethod.GET)
    public @ResponseBody
    VideoStatus
    getVideo(@PathVariable("id") long videoToken, HttpServletResponse response) throws IOException {

        VideoStatus videoStatus = null;
        VideoFileManager videoFileManager = VideoFileManager.get();
        if (videos.containsKey(videoToken) && videoFileManager.hasVideoData(videos.get(videoToken))) {
            videoFileManager.copyVideoData(videos.get(videoToken), response.getOutputStream());
        } else {
            response.setStatus(404);

        }
        return videoStatus;

    }


}

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

package org.magnum.mobilecloud.video;

import com.google.common.collect.Lists;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/video")
public class VideoController {
    @Autowired
    private VideoRepository videoRepository;
    private Logger logger = LoggerFactory.getLogger(VideoController.class);

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


    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody
    Collection<Video> getAllVideos() {


        Collection<Video> videos = (Collection<Video>) videoRepository.findAll();

        for (Video video : videos) {
            logger.info(" GET /video --> Video id : {} ,VideoName: {}, VideoUrl:{} , VideoLikedBy:{},videoDuration:{}", video.getId(), video.getName(), video.getUrl(), video.getLikedBy(), video.getDuration());
        }

        return (Collection) (videoRepository.findAll());
    }

    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody
    Video saveVideo(@RequestBody Video video) {

        logger.info("Post /video ---> Video id : {} ,VideoName:{}, VideoUrl:{} , VideoLikedBy:{},Video duration:{},videoLikes:{}", video.getId(), video.getName(), video.getUrl(), video.getLikedBy(), video.getDuration(),video.getLikes());
        return videoRepository.save(video);

    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public @ResponseBody
    Video getVideo(@PathVariable long id) {
        Video video = videoRepository.findOne(id);
        //logger.info("Get /video/id --> Video id : {} , VideoUrl:{} , VideoLikedBy:{}", video.getId(), video.getUrl(), video.getLikedBy());
        if (video != null)
            return video;
        throw new ResourceNotFoundException("404");
    }

    @RequestMapping(value = "/{id}/like", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity
    likeVideo(@PathVariable long id, Principal principal, HttpServletResponse response) {
        Video video = getVideo(id);
        boolean notAlreadyLiked = video.addLike(principal.getName());
        videoRepository.save(video);
        if (notAlreadyLiked) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

    }

    private void setResponseStatus(HttpServletResponse response, boolean b) {
        if (b) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }


    @RequestMapping(value = "/{id}/unlike", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity unLikeVideo(@PathVariable long id, Principal principal, HttpServletResponse response) {
        Video video = getVideo(id);
        boolean alreadyLiked = video.removeLike(principal.getName());
        videoRepository.save(video);
        if (alreadyLiked) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/search/findByName", method = RequestMethod.GET)
    public @ResponseBody
    Collection<Video> getVideoByName(@RequestParam("title") String name) {
        Collection<Video> videos = videoRepository.findByName(name);
        if (videos == null) {
            return null;
        }
        return videos;
    }

    @RequestMapping(value = "/search/findByDurationLessThan", method = RequestMethod.GET)
    public @ResponseBody
    Collection<Video> getVideosLessThanThisDuration(@RequestParam("duration") long duration) {
        Collection<Video> videos = videoRepository.findByDurationLessThan(duration);
        if (videos == null) {
            return null;
        }
        return videos;
    }

}

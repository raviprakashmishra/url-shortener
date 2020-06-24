package com.ravi.java.urlshortener.controllers;

import java.nio.charset.Charset;

import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.hash.Hashing;
import com.ravi.java.urlshortener.models.Error;
import com.ravi.java.urlshortener.models.Url;

@RestController
@RequestMapping(value = "/rest/url")
public class UrlShortenerController {

  @Autowired
  private RedisTemplate<String, Url> redisTemplate;
  
  @Autowired
  private RedisTemplate<String, String> strRedisTemplate;

  @Value("${redis.ttl}")
  private long ttl;

  /**
   * Returns the original URL.
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity getUrl(@PathVariable String id) {

    // get from redis
    String url = strRedisTemplate.opsForValue().get(id);

    if (url == null) {
      Error error = new Error("id", id, "No such key exists");
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    return ResponseEntity.ok(url);
  }

  /**
   * Returns a short URL.
   */
  @RequestMapping(method = RequestMethod.POST)
  @ResponseBody
  public Url postUrl(@RequestBody Url url) {

    UrlValidator validator = new UrlValidator(
        new String[]{"http", "https"}
    );


    String id = Hashing.murmur3_32().hashString(url.getUrl(), Charset.defaultCharset()).toString();
    url.setUrl(id);

    //store in redis
    strRedisTemplate.opsForValue().set(id, url.getUrl());

    return url;
  }

}

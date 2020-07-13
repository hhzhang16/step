// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;

/** Given meeting information and event information, returns the times when the meeting could happen that day
  */
public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> requiredAttendees = request.getAttendees();
    Collection<String> allAttendees = new HashSet<String>(requiredAttendees);
    allAttendees.addAll(request.getOptionalAttendees());
    int duration = (int) request.getDuration();
    
    Collection<TimeRange> potentialTimesAll = findPotentialTimes(events, allAttendees, duration);
    Collection<TimeRange> potentialTimesOnlyRequired = findPotentialTimes(events, requiredAttendees, duration);
    if (potentialTimesAll.isEmpty() && !requiredAttendees.isEmpty()) return potentialTimesOnlyRequired;
    return potentialTimesAll;
  }

  private Collection<TimeRange> findPotentialTimes(Collection<Event> events, Collection<String> attendees, int duration) {
    ArrayList<TimeRange> eventTimes = attendeeEventTimes(events, attendees);
    ArrayList<TimeRange> unavailableTimes = mergeOverlappingTimes(eventTimes);
    return findDurationsAvailable(unavailableTimes, duration);
  }

  private ArrayList<TimeRange> attendeeEventTimes(Collection<Event> events, Collection<String> attendees) {
    ArrayList<TimeRange> eventTimes = new ArrayList<TimeRange>();
    for (Event event : events) {
      Collection<String> intersectSet = new HashSet<String>(attendees);
      intersectSet.retainAll(event.getAttendees());
      // Someone in this event is also required in the requested event
      if (!intersectSet.isEmpty()) {
          eventTimes.add(event.getWhen());
        }
    }
    return eventTimes;
  }

  /* Combine overlapping ranges to hold a collection of non-overlapping unavailable times */
  private ArrayList<TimeRange> mergeOverlappingTimes(ArrayList<TimeRange> eventTimes) {
    Collections.sort(eventTimes, TimeRange.ORDER_BY_START);
    ArrayList<TimeRange> unavailableTimes = new ArrayList<TimeRange>();
    if (eventTimes.isEmpty()) return unavailableTimes;
    
    TimeRange earlierRange = eventTimes.get(0);
    for (int i = 1; i < eventTimes.size(); i++) {
      TimeRange laterRange = eventTimes.get(i);
      if (earlierRange.overlaps(laterRange)) { 
        // Combine the two ranges
        int newEnd = (earlierRange.end() > laterRange.end()) ? earlierRange.end() : laterRange.end();
        earlierRange = TimeRange.fromStartEnd(earlierRange.start(), newEnd, false);
      } else { 
        // We have found a complete unavailable time range
        unavailableTimes.add(earlierRange);
        earlierRange = laterRange;
      }
    }
    unavailableTimes.add(earlierRange);
    return unavailableTimes;
  }

  private Collection<TimeRange> findDurationsAvailable(ArrayList<TimeRange> unavailableTimes, int duration) {
    // Add all available times that can fit the requested duration
    Collection<TimeRange> potentialTimes = new ArrayList<TimeRange>();
    int startTime = TimeRange.START_OF_DAY;
    int currentEndTime = startTime + duration;
    for (int i = 0; i < unavailableTimes.size(); i++) {
      int beginningNextEvent = unavailableTimes.get(i).start();
      if (currentEndTime <= beginningNextEvent) {
        potentialTimes.add(TimeRange.fromStartEnd(startTime, beginningNextEvent, false));
      }
      startTime = unavailableTimes.get(i).end();
      currentEndTime = startTime + duration;
    }
    if (currentEndTime <= TimeRange.END_OF_DAY) {
      potentialTimes.add(TimeRange.fromStartEnd(startTime, TimeRange.END_OF_DAY, true));
    }
    return potentialTimes;
  }
}
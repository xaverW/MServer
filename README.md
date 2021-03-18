[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](http://www.gnu.org/licenses/gpl-3.0)
[![Build status](https://github.com/mediathekview/MServer/workflows/Build%20and%20test/badge.svg?branch=develop)](https://github.com/mediathekview/MServer/actions?query=workflow%3A%22Build+and+test%22+branch%3Adevelop)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mediathekview_MServer&metric=alert_status)](https://sonarcloud.io/dashboard?id=mediathekview_MServer&branch=develop)

<!-- omit in toc -->

# MServer

MServer is a free open-source software that crawls the media libraries of the German-speaking public broadcasters and prepares them as a machine-readable list for clients such as [MediathekView](https://github.com/mediathekview/MediathekView).

**Hint:** As MediathekView is a German software for the DACH tv
stations ["Öffentlich-Rechtliche"](https://de.wikipedia.org/wiki/%C3%96ffentlich-rechtlicher_Rundfunk), many things like
issue descriptions, UI text and so on are in German. If you don't understand something feel free to ask one of us. Also,
feel free to create issues in English.

<!-- omit in toc -->

## Table of Contents

- [MServer](#mserver)
  - [Table of Contents](#table-of-contents)
  - [Contributing Guide](#contributing-guide)
  - [Code of Conduct](#code-of-conduct)
  - [Repository structure](#repository-structure)
  - [Roadmap](#roadmap)
  - [Architecture Overview](#architecture-overview)

## Contributing Guide

If you want to contribute to the MServer read
the [Contributing Guide](CONTRIBUTING.md).

## Code of Conduct

This project and everyone participates in it is governed by the
[MediathekView Code of Conduct](CODE_OF_CONDUCT.md). By
participating, you are expected to uphold this code. Please report unacceptable behavior to <info@mediathekview.de>.

## Repository structure

Currently, there are two complete different branches. `master` which is the old server and `develop` which is a new
server, completely written from scratch. `m̀aster` will only get updates until develop is stable enough to replace
master.

## Roadmap

Our current roadmap is:

1. Fixing all bigger bugs in `develop` to get it stable enough to replace `master`
2. Create a concrete concept for an API including a database to replace the old film list file. The concept could
   already include how to split the services if not add these before 3. Base this concept on Arc42 and DDD. The API
   should be designed to be useful for all clients. This will be the first step to split all crawler to own services.
   The database should be scanned for dead links, but these entries with dead links shouldn't delete directly. Instead
   of deleting these entries just don't serve them over the API anymore and increase a check counter. Only after a
   configurable amount of checks the entries should be deleted. When to check the links should be configurable too.
3. Implement the design of the step before.
4. Split all crawlers to own services according to the concept of 2.

## Architecture Overview

For an architectural overview see [Architecture Overview](ARCHITECTURE_OVERVIEW.md)
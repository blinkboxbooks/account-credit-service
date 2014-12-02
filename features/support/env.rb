require 'rubygems'
require 'httparty'
require 'blinkbox/user'
require 'securerandom'
require "httpclient/capture"
require "cucumber/rest"
require "cucumber/helpers"
require "cucumber/blinkbox/environment"
require "cucumber/blinkbox/requests"
require "cucumber/blinkbox/responses"
require "cucumber/blinkbox/response_validation"
require "cucumber/blinkbox/data_dependencies"

# Configure test properties
TEST_CONFIG['server'] = ENV['SERVER'] || 'dev_int'
TEST_CONFIG['debug'] = ENV['DEBUG']

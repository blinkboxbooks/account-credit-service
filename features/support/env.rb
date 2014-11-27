require 'rubygems'
require 'httparty'
require 'blinkbox/user'
require 'securerandom'

# Configure test properties
env = ENV['SERVER'] || 'dev_int'
$env_properties = YAML.load_file('config/environments.yml')[env]

def credit_host
  $env_properties['services']['credit']
end

def health_check_host
  $env_properties['services']['health']
end

def api_username
  $env_properties['users']['api_user']['username']
end

def api_password
  $env_properties['users']['api_user']['password']
end

def csr_username
  $env_properties['users']['csr_user']['username']
end

def csr_password
  $env_properties['users']['csr_user']['password']
end

def csm_username
  $env_properties['users']['csm_user']['username']
end

def csm_password
  $env_properties['users']['csm_user']['password']
end

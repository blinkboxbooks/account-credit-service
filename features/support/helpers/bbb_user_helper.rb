module KnowsAboutBbbUsers
  attr_accessor :last_admin_user, :last_public_user

  def use_api_user
    user = data_for_a(:user, which: "is an api user")
    @last_admin_user = authenticate_user(user['username'], user['password'])
  end

  def use_admin_user(role)
    user = data_for_a(:user, which: "has the #{role.to_s.downcase} role")
    @last_admin_user = authenticate_user(user['username'], user['password'])
  end

  def new_public_user
    public_user = Blinkbox::User.new(:username => random_email, :password => 'abc123', :server_uri => test_env.servers['auth'])
    public_user.register
    public_user.authenticate
    @last_public_user = public_user
  end

  def use_unknown_customer
    @last_public_user = Blinkbox::User.new(:username => 'unknown-user@bbb.com', :password => 'abc123', :server_uri => test_env.servers['auth'])
    @last_public_user.instance_eval %Q"
      def user_id
        '99999999999'
      end
    "
  end

  def use_admin_user_without_permissions
    @last_admin_user = Blinkbox::User.new(:username => random_email, :password => 'abc123', :server_uri => test_env.servers['auth'])
    @last_admin_user.register
    @last_admin_user.authenticate
  end

  def use_logged_out_admin_user
    @last_admin_user = Blinkbox::User.new(:username => 'logged-out-user@bbb.com', :password => 'abc123', :server_uri => test_env.servers['auth'])
    @last_admin_user.instance_eval %Q"
      def access_token
        false
      end
    "
  end

  def user_id_of(user)
    /\d+/.match(user.user_id).to_s
  end

  def authenticate_user(username, password)
    user = Blinkbox::User.new(:username => username, :password => password, :server_uri => test_env.servers['auth'])
    user.authenticate
    user
  end
end

World(KnowsAboutBbbUsers)

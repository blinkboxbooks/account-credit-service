module KnowsAboutBbbUsers
  attr_accessor :last_admin_user, :last_public_user

  def use_api_user
    # @user = data_for_a(:user, which: "has the #{role} role")
    # @user['username'], password: @user['password'])
    @last_admin_user = authenticate_user(api_username, api_password)
  end

  def use_csm_user
    @last_admin_user = authenticate_user(csm_username, csm_password)
  end

  def use_csr_user
    @last_admin_user = authenticate_user(csr_username, csr_password)
  end

  def new_public_user
    public_user = Blinkbox::User.new(:username => random_email, :password => 'abc123')
    public_user.register
    public_user.authenticate
    @last_public_user = public_user
  end

  def use_unknown_user
    @last_public_user = Blinkbox::User.new(:username => 'unknown-user@bbb.com', :password => 'abc123')
    @last_public_user.instance_eval %Q"
      def user_id
        '999999'
      end
    "
  end

  def user_id_of(user)
    /\d+/.match(user.user_id).to_s
  end

  def authenticate_user(username, password)
    user = Blinkbox::User.new(:username => username, :password => password)
    user.authenticate
    user
  end
end
World(KnowsAboutBbbUsers)

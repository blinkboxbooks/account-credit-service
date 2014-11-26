module KnowsAboutBbbUsers
  @@csm_user = Blinkbox::User.new(:username => csm_username, :password => csm_password)
  @@csm_user.authenticate

  @@csr_user = Blinkbox::User.new(:username => csr_username, :password => csr_password)
  @@csr_user.authenticate

  @@api_user = Blinkbox::User.new(:username => csr_username, :password => csr_password)
  @@api_user.authenticate

  @@last_admin_user
  @@last_public_user

  def use_api_user
    @@api_user
  end

  def use_csm_user
    @@last_admin_user = @@csm_user
  end

  def use_csr_user
    @@last_admin_user = @@csr_user
  end

  def new_public_user
    public_user = Blinkbox::User.new(:username => random_email, :password => 'abc123')
    public_user.register
    public_user.authenticate
    @@last_public_user = public_user
  end

  def use_unknown_user
    @@last_public_user = Blinkbox::User.new(:username => 'unknown-user@bbb.com', :password => 'abc123')
    @@last_public_user.instance_eval %Q"
      def user_id
        '999999'
      end
    "
  end

  def last_admin_user
    @@last_admin_user
  end

  def last_public_user
    @@last_public_user
  end

  def user_id_of(user)
    /\d+/.match(user.user_id).to_s
  end
end
World(KnowsAboutBbbUsers)

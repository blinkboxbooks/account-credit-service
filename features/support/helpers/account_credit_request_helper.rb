require 'json'

module KnowsAboutAccountCreditRequests
  @@host = test_host

  def get_admin_account_credit(admin_auth_token, user_id)
    @response = HTTParty.get("/#{@@host}admin/users/#{user_id}/accountcredit",
                            :options => { :headers => get_headers(admin_auth_token)})
  end

  def post_admin_account_debit(admin_auth_token, amount_debit, user_id, transaction_id)
      @response = HTTParty.post("#{@@host}admin/users/#{user_id}/accountcredit/debits",
      :query => {
                  :requestId => transaction_id,
                  :amount => {
                      :currency => "GBP",
                      :value => amount_debit }},
      :options => { :headers => get_headers(admin_auth_token)})
  end

  def post_admin_account_credit(admin_auth_token, amount, reason, user_id, transaction_id)
    @response = HTTParty.post("#{@@host}admin/users/#{user_id}/accountcredit/credits",
    :query => {
                :requestId => transaction_id,
                :amount => {
                    :currency => "GBP",
                    :value => amount },
                :reason => reason,
                :type => 'credit'},
    :options => { :headers => get_headers(admin_auth_token)})
  end

  def last_response
    @response
  end

  def get_headers(token)
    if token.empty? || token.nil?
      { 'Content-Type' => 'application/vnd.blinkboxbooks.data.v1+json' }
    else
      { 'Content-Type' => 'application/vnd.blinkboxbooks.data.v1+json',
        :Authorization => "Bearer #{token}" }
    end
  end
end

World(KnowsAboutAccountCreditRequests)

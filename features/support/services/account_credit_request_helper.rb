require 'json'

module KnowsAboutAccountCreditRequests
  def get_health_check
    http_get :health, "/health/ping", :Accept => 'text/plain; charset=UTF-8'
  end

  def get_admin_account_credit(admin_auth_token, user_id)
    @access_token = admin_auth_token
    http_get :credit, "/admin/users/#{user_id}/accountcredit"
  end

  def post_admin_account_debit(admin_auth_token, amount_debit, user_id, transaction_id)
    @access_token = admin_auth_token
    request_body = {
      :requestId => transaction_id,
      :amount => {
        :currency => "GBP",
        :value => amount_debit
      }
    }

    http_post :credit, "/admin/users/#{user_id}/accountcredit/debits", request_body
  end

  def post_admin_account_credit(admin_auth_token, amount, reason, user_id, transaction_id)
    @access_token = admin_auth_token
    request_body = {
      :requestId => transaction_id,
      :amount => {
        :currency => "GBP",
        :value => amount
      },
      :reason => reason,
      :type => 'credit'
    }

    http_post :credit, "/admin/users/#{user_id}/accountcredit/credits", request_body
  end
end
World(KnowsAboutAccountCreditRequests)

library ieee;

use ieee.std_logic_1164.all;

entity KeyControl is port(
	CLK: in std_logic;
	K_SCAN: out std_logic;
	K_VAL: out std_logic;
	RESET: in std_logic;
	K_ACK: in std_logic;
	K_PRESS: in std_logic
);
end KeyControl;

architecture behavioral of KeyControl is 
type STATE_TYPE is (STATE_I, STATE_M, STATE_F, STATE_FI);

signal CURRENT_STATE, NEXT_STATE : STATE_TYPE;

begin
CURRENT_STATE<= STATE_I when RESET='1' else NEXT_STATE when rising_edge(clk);

GENERATENEXTSTATE:
process (CURRENT_STATE,K_ACK, K_PRESS)
	begin
	case CURRENT_STATE is
		when STATE_I => if (K_PRESS='1') then 
			                NEXT_STATE<= STATE_M;
							 else 
								 NEXT_STATE <= STATE_I;
							 end if;
		when STATE_M => if (K_ACK='0') then 
			                NEXT_STATE<= STATE_M;
							 else 
								 NEXT_STATE <= STATE_F;
							 end if;
		when STATE_F => if(K_ACK='1') then
								NEXT_STATE<= STATE_F;
							else 
								NEXT_STATE<= STATE_FI;
							end if;
		when STATE_FI => if(K_PRESS='1') then 
								NEXT_STATE <= STATE_F;
								else 
								NEXT_STATE <= STATE_I;
								end if;
		end case;
end process;    
K_SCAN<= '1' when ((CURRENT_STATE = STATE_I))
			else '0';
K_VAL<= '1' when ((CURRENT_STATE = STATE_M))
			else '0';
end behavioral;
							    
			  
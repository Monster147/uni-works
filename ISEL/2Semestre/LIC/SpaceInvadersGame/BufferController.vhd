library ieee;
use ieee.std_logic_1164.all;

entity BufferController is
	port(
	Load: in std_logic;
	ACK: in std_logic;
	clk: in std_logic;
	reset: in std_logic;
	Wreg: out std_logic;
	OBfree: out std_logic;
	Dval: out std_logic
	);
end BufferController;

architecture behavioral of BufferController is 
type STATE_TYPE is (STATE_I, STATE_MI, STATE_MF, STATE_F);

signal CURRENT_STATE, NEXT_STATE : STATE_TYPE;

begin
CURRENT_STATE<= STATE_I when reset='1' else NEXT_STATE when rising_edge(clk);

GENERATENEXTSTATE:
process (CURRENT_STATE, Load, ACK)
	begin
	case CURRENT_STATE is
		when STATE_I => if (Load = '1') then
							NEXT_STATE <= STATE_MI;
							else
							NEXT_STATE <= STATE_I;
							end if;
		when STATE_MI => NEXT_STATE <= STATE_MF;
		when STATE_MF => if (ACK = '1') then
							NEXT_STATE <= STATE_F;
							else
							NEXT_STATE <= STATE_MF;
							end if;
		when STATE_F => if (ACK = '1') then
							NEXT_STATE <= STATE_F;
							else
							NEXT_STATE <= STATE_I;
							end if;
		end case;
end process;
OBfree<= '1' when ((CURRENT_STATE = STATE_I))
			else '0';
Wreg<= '1' when ((CURRENT_STATE = STATE_MI))
			else '0';
Dval<= '1' when ((CURRENT_STATE = STATE_MF))
			else '0';
end behavioral;
library ieee;

use ieee.std_logic_1164.all;

entity ScoreDispatcherControl is port(
	Dval: in std_logic;
	RESET: in std_logic;
	WrD: out std_logic;
	done: out std_logic;
	CLK: in std_logic
);
end ScoreDispatcherControl;

architecture behavioral of ScoreDispatcherControl is 
type STATE_TYPE is (STATE_I, STATE_M, STATE_F);

signal CURRENT_STATE, NEXT_STATE : STATE_TYPE;
begin
CURRENT_STATE<= STATE_I when RESET='1' else NEXT_STATE when rising_edge(CLK);

GENERATENEXTSTATE:
process (CURRENT_STATE,Dval)
	begin
	case CURRENT_STATE is							 
		when STATE_I => if (Dval='0') then
							 NEXT_STATE <= STATE_I;
							 else 
							 NEXT_STATE <= STATE_M;
							 end if;
							 
		when STATE_M => NEXT_STATE <= STATE_F;
		
		when STATE_F => if (Dval='1') then
							NEXT_STATE<= STATE_F; 
							else
							NEXT_STATE<= STATE_I;
							end if;

		end case;
end process;    
WrD<= '1' when ((CURRENT_STATE = STATE_M))
			else '0';
done<= '1' when ((CURRENT_STATE = STATE_F))
			else '0';
end behavioral;